package com.cniao.test_application.ui.notifications

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.cniao.test_application.ActivityList
import com.cniao.test_application.R
import com.cniao.test_application.entity.Course
import com.cniao.test_application.databinding.FragmentNotificationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var isEducator = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.addCourse.setOnClickListener {
            addCourseDialog()
        }
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { result ->
            for (document in result) {
                val data = document.data
                val identity = data["identity"].toString()
                if (identity == "Educator") {
                    isEducator = true
                    findCourseByEduEmail()
                } else {
                    root.findViewById<Button>(R.id.addCourse).visibility = View.INVISIBLE
                    findCourseByStuEmail()
                }
            }
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun findCourseByStuEmail() {
        val db = Firebase.firestore
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val ref = db.collection("CourseSelection").document(email)
        ref.get().addOnSuccessListener { result ->
            val data = result.data?.toMutableMap()
            if (data != null) {
                for ((k, v) in data) {
                    addCourseLayout(Course(k, v.toString()))
                }
            }
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, number, resources.displayMetrics
        ).toInt()
    }

    private fun addCourseLayout(course: Course) {
        val view = binding.root.findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this.context)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layout.layoutParams = layoutLp
        val textView = TextView(this.context)
        val textViewLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(90f)
        )
        textView.text = course.course_name
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30f)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = textViewLp
        textView.setBackgroundColor(Color.LTGRAY)
        textView.setOnClickListener {
            val intent =
                Intent(requireActivity(), ActivityList::class.java).putExtra("course_id", course.id)
                    .putExtra("course_name", course.course_name)
                    .putExtra("edu_email", course.edu_email).putExtra("isEducator", isEducator)
            startActivity(intent)
        }
        layout.addView(textView)
        view.addView(layout)
    }

    private fun addCourseDialog() {
        val layout = LinearLayout(binding.root.context)
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(10f)
        lp.leftMargin = dp(10f)
        lp.rightMargin = dp(10f)
        val editText = EditText(binding.root.context)
        editText.hint = "Please input the course name..."
        editText.layoutParams = lp
        layout.addView(editText)
        val alert = AlertDialog.Builder(binding.root.context).setView(layout).setCancelable(false)
            .setNegativeButton("Cancel") { _, _ -> }.setPositiveButton("Submit") { _, _ -> }
            .create()
        alert.show()
        val submitButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        submitButton.setOnClickListener {
            if (editText.length() == 0) {
                editText.error = "This field is required!!!"
            } else {
                submit(editText.text.toString())
                alert.cancel()
            }
        }
    }

    private fun submit(courseName: String) {
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val course = Course(null, courseName, email)
        val db = Firebase.firestore
        db.collection("Course").add(course).addOnSuccessListener {
            Toast.makeText(
                requireContext(), "Add Course successfully!!!", Toast.LENGTH_LONG
            ).show()
            binding.root.findNavController().navigate(R.id.navigation_notifications)
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun findCourseByEduEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore
        db.collection("Course").whereEqualTo("edu_email", user!!.email.toString()).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    addCourseLayout(
                        Course(
                            document.id,
                            data["course_name"].toString(),
                            data["edu_email"].toString(),
                        )
                    )
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}