package com.cniao.test_application

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cniao.test_application.entity.Question
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ActivityDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val id = intent.getStringExtra("activity_id").toString()
        val activityName = intent.getStringExtra("activity_name").toString()
        val availableTime = intent.getStringExtra("available_time").toString()
        findViewById<TextView>(R.id.activityName).text = activityName
        findViewById<TextView>(R.id.availableTime).text = availableTime
        findViewById<Button>(R.id.addQuestion).setOnClickListener {
            val intent = Intent(this, QuestionActivity::class.java)
            intent.putExtra("activity_id", id)
            intent.putExtra("activity_name", activityName)
            intent.putExtra("available_time", availableTime)
            finish()
            startActivity(intent)
        }
        findQuestionByActivityId(id)
        supportActionBar?.title = "Activity Detail"
    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            number,
            resources.displayMetrics
        ).toInt()
    }

    private fun findQuestionByActivityId(id: String) {
        val db = Firebase.firestore
        db.collection("Question")
            .whereEqualTo("activity_id", id)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val data = document.data
                    addQuestionLayout(
                        Question(
                            document.id,
                            id,
                            data["description"].toString(),
                        )
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun addQuestionLayout(question: Question) {
        val view = this.findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layoutLp.leftMargin = dp(15f)
        layoutLp.rightMargin = dp(15f)
        layout.layoutParams = layoutLp
        val textView = TextView(this)
        val textViewLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(90f)
        )
        textView.text = question.description
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        textView.layoutParams = textViewLp
        textView.setBackgroundColor(Color.LTGRAY)
        textView.setOnClickListener {
            val intent = Intent(this, QuestionActivity::class.java)
                .putExtra("question_id", question.id)
            startActivity(intent)
        }
        layout.addView(textView)
        view.addView(layout)
    }
}