package com.cniao.test_application.ui.dashboard

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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.cniao.test_application.R
import com.cniao.test_application.StudentActivity
import com.cniao.test_application.databinding.FragmentDashboardBinding
import com.cniao.test_application.entity.Activity
import com.cniao.test_application.entity.Course
import com.cniao.test_application.entity.CourseSelection
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat


class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private var isEducator = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email.toString()
        val db = Firebase.firestore
        db.collection("users")
            .whereEqualTo("email", email).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val identity = data["identity"].toString()
                    if (identity == "Educator") {
                        isEducator = true
                        findCourseByEduEmail()
                    } else {
                        findUnSelectedCourse()
                    }
                }
            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
        return root
    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            number,
            resources.displayMetrics
        ).toInt()
    }

    private fun findUnSelectedCourse() {
        val db = Firebase.firestore
        db.collection("Course").get()
            .addOnSuccessListener { result ->
                val courseList = mutableListOf<Course>()
                for (document in result) {
                    val data = document.data
                    courseList.add(
                        Course(
                            document.id,
                            data["course_name"].toString(),
                            data["edu_email"].toString(),
                        )
                    )
                }
                findCourseByStuEmail(courseList)
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun findCourseByStuEmail(courseList: List<Course>) {
        val db = Firebase.firestore
        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
        val ref = db.collection("CourseSelection").document(email)
        ref.get().addOnSuccessListener { result ->
            val data = result.data?.toMutableMap()
            for (course in courseList) {
                var find = false
                if (data != null) {
                    for ((k, v) in data) {
                        if (course.id == k) {
                            find = true
                        }
                    }
                    if (!find) {
                        addCourseLayout(course)
                    }
                } else {
                    addCourseLayout(course)
                }

            }
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun addCourseLayout(course: Course) {
        val view = binding.root.findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this.context)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layout.layoutParams = layoutLp
        val textView = TextView(this.context)
        val textViewLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(90f)
        )
        textView.text = course.course_name
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30f)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = textViewLp
        textView.setBackgroundColor(Color.LTGRAY)
        textView.setOnClickListener {
            if (isEducator) {
                val intent = Intent(requireActivity(), StudentActivity::class.java)
                intent.putExtra("course_id", course.id)
                intent.putExtra("course_name", course.course_name)
                intent.putExtra("isEducator", isEducator)
                startActivity(intent)
            } else {
                selectCourse(course.id.toString(), course.course_name)
            }
        }
        layout.addView(textView)
        view.addView(layout)
    }

    private fun selectCourse(courseId: String, courseName: String) {
        AlertDialog.Builder(requireActivity())
            .setMessage("Are you sure you want to choose this course?") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(
                "Submit"
            ) { dialog, _ ->
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email.toString()
                val db = Firebase.firestore
                val ref = db.collection("CourseSelection").document(email)
                val data = hashMapOf(
                    courseId to courseName
                )
                ref.set(data, SetOptions.merge())
                db.collection("CourseSelection")
                    .add(CourseSelection(email, courseId))
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Choose $courseName successfully!!!",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.root.findNavController().navigate(R.id.navigation_dashboard)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(ContentValues.TAG, "Error getting documents.", exception)
                    }
                dialog.cancel()
            }.setNegativeButton("Cancel") { dialig, _ -> dialig.cancel() }
            .show()
    }

//    private fun findCourseByEduEmail() {
//        val email = FirebaseAuth.getInstance().currentUser?.email.toString()
//        val db = Firebase.firestore
//        db.collection("Course").whereEqualTo("edu_email", email).get()
//            .addOnSuccessListener { result ->
//                val idList = mutableListOf<String>()
//                val courseList = mutableListOf<Course>()
//                for (document in result) {
//                    idList.add(document.id)
//                    val data = document.data
//                    courseList.add(Course(document.id, data["course_name"].toString()))
//                }
//                findActivityByCourseIds(idList, courseList)
//            }.addOnFailureListener { exception ->
//                Log.w(ContentValues.TAG, "Error getting documents.", exception)
//            }
//    }

    private fun findCourseByEduEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore
        db.collection("Course")
            .whereEqualTo("edu_email", user!!.email.toString())
            .orderBy("add_time", Query.Direction.DESCENDING)
            .get()
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
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun findActivityByCourseIds(idList: List<String>, courseList: List<Course>) {
        val db = Firebase.firestore
        if (idList.isEmpty()) {
            return
        }
        db.collection("Activity")
            .whereIn("course_id", idList)
            .orderBy("available_time", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val date = data["available_time"] as Timestamp
                    for (course in courseList) {
                        if (course.id == data["course_id"]) {
                            addActivityView(
                                Activity(
                                    null,
                                    data["course_id"].toString(),
                                    data["activity_name"].toString(),
                                    date.toDate()
                                ), course.course_name
                            )
                        }
                    }

                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun addActivityView(activity: Activity, courseName: String) {
        val view = binding.root.findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this.context)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layout.layoutParams = layoutLp
        val textView = TextView(this.context)
        val textViewLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(90f)
        )
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm")
        textView.text =
            courseName + " : " + activity.activity_name + " - " + dateFormat.format(activity.available_time)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = textViewLp
        textView.setBackgroundColor(Color.LTGRAY)
        textView.setOnClickListener {
//            selectCourse(course.id.toString(), course.course_name)
        }
        layout.addView(textView)
        view.addView(layout)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}