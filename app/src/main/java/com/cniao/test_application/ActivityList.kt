package com.cniao.test_application

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cniao.test_application.entity.Activity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_detail.activityName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class ActivityList : AppCompatActivity() {
    private var isEducator = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val courseId = intent.getStringExtra("course_id").toString()
        val courseName = intent.getStringExtra("course_name").toString()
        val studentEmail = intent.getStringExtra("student_email").toString()
        isEducator = intent.getBooleanExtra("isEducator", false)
        if (!isEducator || studentEmail != "null") {
            findViewById<Button>(R.id.addActivity).visibility = View.INVISIBLE
        }
        findViewById<TextView>(R.id.courseName).text = courseName
        findViewById<Button>(R.id.addActivity).setOnClickListener {
            addActivityDialog(courseId)
        }
        if (studentEmail == "null") {
            findActivityByCourseId(courseId, studentEmail)
        } else if (isEducator && studentEmail != "null") {
            findActivityByEmail(courseId, studentEmail)
        }

        supportActionBar?.title = "Activity List"

    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, number, resources.displayMetrics
        ).toInt()
    }

    private fun findActivityByCourseId(id: String, studentEmail: String) {
        val db = Firebase.firestore
        db.collection("Activity").whereEqualTo("course_id", id)
//            .orderBy("available_time")
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val date = data["available_time"] as Timestamp
                    addActivityLayout(
                        Activity(
                            document.id, id, data["activity_name"].toString(), date.toDate()
                        ), studentEmail
                    )
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun findActivityByEmail(id: String, studentEmail: String) {
        val db = Firebase.firestore
        db.collection("Result").whereEqualTo("email", studentEmail).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    val date = data["available_time"] as Timestamp
                    addActivityLayout(
                        Activity(
                            data["activity_id"].toString(),
                            id,
                            data["activity_name"].toString(),
                            date.toDate()
                        ), studentEmail
                    )
                }
            }.addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

//    private fun findActivityByEmail(id: String) {
//        val db = Firebase.firestore
//        db.collection("Activity").whereEqualTo("course_id", id)
////            .orderBy("available_time")
//            .get().addOnSuccessListener { result ->
//                for (document in result) {
//                    val data = document.data
//                    val date = data["available_time"] as Timestamp
//                    addActivityLayout(
//                        Activity(
//                            document.id, id, data["activity_name"].toString(), date.toDate()
//                        )
//                    )
//                }
//            }.addOnFailureListener { exception ->
//                Log.w(ContentValues.TAG, "Error getting documents.", exception)
//            }
//    }

    private fun addActivityLayout(activity: Activity, studentEmail: String) {
        val view = this.findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layoutLp.leftMargin = dp(15f)
        layoutLp.rightMargin = dp(15f)
        layout.layoutParams = layoutLp
        val textView = TextView(this)
        val textViewLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(90f)
        )
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm")
        val datetime = dateFormat.format(activity.available_time)
        textView.text = activity.activity_name + " - " + datetime
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = textViewLp
        textView.setBackgroundColor(Color.LTGRAY)
        textView.setOnClickListener {
            if (isEducator && studentEmail == "null") {
                val intent = Intent(this, ActivityDetail::class.java).putExtra(
                    "activity_id", activity.id
                ).putExtra("activity_name", activity.activity_name)
                    .putExtra("available_time", datetime)
                startActivity(intent)
            } else {
                exam(activity.id.toString(), activity.activity_name, datetime, studentEmail)
            }
        }
        layout.addView(textView)
        view.addView(layout)
    }

    private fun exam(
        activityId: String,
        activityName: String,
        availableTime: String,
        studentEmail: String
    ) {
        var email = FirebaseAuth.getInstance().currentUser?.email.toString()
        if (isEducator) {
            email = studentEmail
        }
        val db = Firebase.firestore
        val ref = db.collection("Result").document(activityId).collection(email)
        ref.get().addOnSuccessListener { result ->
            if (result.documents.size == 0) {
                val intent =
                    Intent(this, QuizActivity::class.java).putExtra("activity_id", activityId)
                        .putExtra("userEmail", email).putExtra("activity_name", activityName)
                        .putExtra("available_time", availableTime)
                startActivity(intent)
            } else {
                val intent = Intent(this, QuizResultActivity::class.java).putExtra(
                    "activityName", activityId
                ).putExtra("userEmail", email)
                startActivity(intent)
            }
        }.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addActivityDialog(id: String) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(10f)
        lp.leftMargin = dp(10f)
        lp.rightMargin = dp(10f)
        val activityName = EditText(this)
        activityName.hint = "Please input the activity name..."
        activityName.layoutParams = lp
        val date = EditText(this)
        date.inputType = EditorInfo.TYPE_CLASS_DATETIME
        date.focusable = View.NOT_FOCUSABLE
        date.hint = "Click to choose date"
        date.setOnClickListener {
            dateDialog(date)
        }
        date.layoutParams = lp
        val time = EditText(this)
        time.inputType = EditorInfo.TYPE_CLASS_DATETIME
        time.focusable = View.NOT_FOCUSABLE
        time.hint = "Click to choose time"
        time.setOnClickListener {
            timeDialog(time)
        }
        time.layoutParams = lp
        layout.addView(activityName)
        layout.addView(date)
        layout.addView(time)
        val alert = AlertDialog.Builder(this).setView(layout)
//            .setCancelable(false)
//            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Submit") { _, _ -> }.create()
        alert.show()
        val submitButton = alert.getButton(AlertDialog.BUTTON_POSITIVE)
        submitButton.setOnClickListener {
            val b1 = validate(activityName)
            val b2 = validate(date)
            val b3 = validate(time)
            if (b1 && b2 && b3) {
                submit(
                    activityName.text.toString(),
                    id,
                    getDateTime(date.text.toString(), time.text.toString())
                )
                alert.cancel()
            }
        }
    }

    private fun validate(editText: EditText): Boolean {
        if (editText.length() == 0) {
            editText.error = "This field is required!!!"
            return false
        }
        return true
    }

    private fun getDateTime(date: String, time: String): Date {
        val dateFormat1 = SimpleDateFormat("MM/dd/yyyy")
        val dateFormat2 = SimpleDateFormat("HH:mm")
        val datetime = dateFormat1.parse(date).time + dateFormat2.parse(time).time
        return Date(datetime)
    }

    private fun dateDialog(editText: EditText) {
        val date = DatePickerDialog(this)
        date.setOnDateSetListener { _, year, month, day ->
            val instance = Calendar.getInstance()
            instance.set(year, month, day)
            val dateFormat = SimpleDateFormat("MM/dd/yyyy")
            editText.setText(dateFormat.format(instance.time))
        }
        date.show()
    }

    private fun timeDialog(editText: EditText) {
        val time = TimePickerDialog(
            this, { _, hour, minute ->
                var temp = if (minute < 10) "0${minute}" else "$minute"
                editText.setText("${hour}:${temp}")
            }, 9, 0, true
        )
        time.show()
    }

    private fun submit(activityName: String, courseId: String, availableTime: Date) {
        val activity = Activity(null, courseId, activityName, availableTime)
        val db = Firebase.firestore
        db.collection("Activity").add(activity).addOnSuccessListener {
            Toast.makeText(
                this, "Add Activity successfully!!!", Toast.LENGTH_LONG
            ).show()
            val intent = Intent(this, ActivityList::class.java)
            intent.putExtra("course_id", courseId)
            intent.putExtra("course_name", findViewById<TextView>(R.id.courseName).text)
            intent.putExtra("isEducator", isEducator)
            finish()
            startActivity(intent)
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }
}