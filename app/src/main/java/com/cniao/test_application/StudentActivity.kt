package com.cniao.test_application

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.children
import com.cniao.test_application.entity.CourseSelection
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)
        supportActionBar?.title = "Add Student"
        val courseId = intent.getStringExtra("course_id").toString()
        val courseName = intent.getStringExtra("course_name").toString()
        findViewById<TextView>(R.id.courseName).text = courseName
        findStudentByCourseId(courseId, courseName)
        val addList = mutableListOf<String>()
        findViewById<Button>(R.id.addStudent).setOnClickListener {
            addList.clear()
            val tableLayout = findViewById<TableLayout>(R.id.tablelayout)
            for (children in tableLayout.children) {
                val left = (children as TableRow).getChildAt(0) as LinearLayout
                val right = (children as TableRow).getChildAt(2) as LinearLayout
                if (left.childCount > 0) {
                    val checkBox1 = left.getChildAt(0) as CheckBox
                    if (checkBox1.isChecked && checkBox1.isEnabled) {
                        addList.add((left.getChildAt(1) as TextView).text.toString())
                    }
                }
                if (right.childCount > 0) {
                    val checkBox2 = right.getChildAt(0) as CheckBox
                    if (checkBox2.isChecked && checkBox2.isEnabled) {
                        addList.add((right.getChildAt(1) as TextView).text.toString())
                    }
                }
            }
            if (addList.size > 0) {
                submit(addList, courseId, courseName)
            }
        }
    }

    private fun batchSubmit(list: List<String>, courseId: String, courseName: String) {
        val db = Firebase.firestore
        db.runBatch {
            for (email in list) {
                val collection = db.collection("CourseSelection").document()
                it.set(collection, CourseSelection(email, courseId))
            }
        }.addOnCompleteListener {
            Toast.makeText(
                this, "Add Student successfully!!!", Toast.LENGTH_LONG
            ).show()
            val intent = Intent(this, StudentActivity::class.java)
            intent.putExtra("course_id", courseId)
            intent.putExtra("course_name", courseName)
            finish()
            startActivity(intent)
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun submit(list: List<String>, courseId: String, courseName: String) {
        val db = Firebase.firestore
        db.runBatch {
            for (email in list) {
                val ref = db.collection("CourseSelection").document(email)
                val data = hashMapOf(
                    courseId to courseName
                )
                it.set(ref, data, SetOptions.merge())
            }
        }.addOnCompleteListener {
            batchSubmit(list, courseId, courseName)
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, number, resources.displayMetrics
        ).toInt()
    }


    private fun findAllStudent(emailList: List<String>, courseId: String, courseName: String) {
        val db = Firebase.firestore
        db.collection("users").whereEqualTo("identity", "Learner").get()
            .addOnSuccessListener { result ->
                val documents = result.documents
                var index = 0
                for (i in 0 until documents.size) {
                    if (index == documents.size) break
                    val email1 = documents[index++].data?.get("email").toString()
                    if (documents.size == 1) {
                        addStudentCheckBox(
                            email1, "", haveEmail(email1, emailList), false, courseId, courseName
                        )
                        break
                    }
                    val email2 = documents[index++].data?.get("email").toString()
                    addStudentCheckBox(
                        email1,
                        email2,
                        haveEmail(email1, emailList),
                        haveEmail(email2, emailList),
                        courseId,
                        courseName
                    )
                }

            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun haveEmail(email: String, emailList: List<String>): Boolean {
        for (e in emailList) {
            if (e == email) {
                return true
            }
        }
        return false
    }

    private fun findStudentByCourseId(courseId: String, courseName: String) {
        val db = Firebase.firestore
        db.collection("CourseSelection").whereEqualTo("course_id", courseId).get()
            .addOnSuccessListener { result ->
                val emailList = mutableListOf<String>()
                for (document in result) {
                    val data = document.data
                    emailList.add(data["email"].toString())
                }
                findAllStudent(emailList, courseId, courseName)
            }.addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun addStudentCheckBox(
        email1: String,
        email2: String,
        haveEmail1: Boolean,
        haveEmail2: Boolean,
        courseId: String,
        courseName: String
    ) {
        val tablelayout = findViewById<TableLayout>(R.id.tablelayout)
        val tableRow = TableRow(this)
        val linearLayout1 = LinearLayout(this)
        val checkbox1 = CheckBox(this)
        val textview1 = TextView(this)
        val linearLayout2 = LinearLayout(this)
        val checkbox2 = CheckBox(this)
        val textview2 = TextView(this)
        textview1.text = email1
        textview2.text = email2
        if (haveEmail1) {
            checkbox1.isChecked = true
            checkbox1.isEnabled = false
            textview1.setOnClickListener {
                val email = (it as TextView).text
                val intent = Intent(this, ActivityList::class.java)
                intent.putExtra("student_email", email)
                intent.putExtra("course_id", courseId)
                intent.putExtra("course_name", courseName)
                intent.putExtra("isEducator", true)
                startActivity(intent)
            }
        }
        if (haveEmail2) {
            checkbox2.isChecked = true
            checkbox2.isEnabled = false
            textview2.setOnClickListener {
                val email = (it as TextView).text
                val intent = Intent(this, ActivityList::class.java)
                intent.putExtra("student_email", email)
                intent.putExtra("course_id", courseId)
                intent.putExtra("course_name", courseName)
                intent.putExtra("isEducator", true)
                startActivity(intent)
            }
        }
        linearLayout1.addView(checkbox1)
        linearLayout1.addView(textview1)
        tableRow.addView(linearLayout1)
        tableRow.addView(TextView(this))
        if (email2 != "") {
            linearLayout2.addView(checkbox2)
            linearLayout2.addView(textview2)
        }
        tableRow.addView(linearLayout2)
        tablelayout.addView(tableRow)
    }
}