package com.cniao.test_application

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.TreeMap

class QuizResultActivity : AppCompatActivity() {
    private var userEmail: String? = null
    private var activityName: String? = null
    private val answerList: MutableList<String?> = ArrayList()
    private val userAnswerList: MutableList<String?> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_result)
        supportActionBar?.title = "Quiz Result"
        val totalQuestionsTv = findViewById<TextView>(R.id.totalQuestionsTv)
        val correctTv = findViewById<TextView>(R.id.correctTv)
        val incorrectTv = findViewById<TextView>(R.id.incorrectTv)
        val viewAnswersBtn = findViewById<AppCompatButton>(R.id.viewAnswersBtn)

        //get parameter from quizactivity
        userEmail = intent.getStringExtra("userEmail")
        activityName = intent.getStringExtra("activityName")

        val db = Firebase.firestore
        //get answerList
        db.collection("Activity").document(activityName!!).collection("questions")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val answer = document.data["answer"]
                    answerList.add(answer as String?)
                }
                //load question numbers
                totalQuestionsTv.text = "" + answerList.size
                subSelect(correctTv, incorrectTv)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }



        //view button listener
        viewAnswersBtn.setOnClickListener {
            val intent = Intent(this@QuizResultActivity, AnswersActivity::class.java)
            intent.putExtra("userEmail", userEmail)
            intent.putExtra("activityName", activityName)
            startActivity(intent)
        }
    }

    fun subSelect(correctTv: TextView, incorrectTv: TextView) {
        val db = Firebase.firestore
        db.collection("Result").document(activityName!!).collection(userEmail!!)
            .document("answer")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val sortedData = TreeMap<String, Any>(data)
                        for (entry in sortedData.entries) {
                            val key = entry.key
                            val value = entry.value
                            userAnswerList.add(value as String?)
                            // Do something with the key-value pair
                            Log.d(TAG, "Key: $key, Value: $value")
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
                //compare user answers to question
                val correctCount = answerList.zip(userAnswerList).count { it.first == it.second }
                val incorrectCount = answerList.size - correctCount
                //set correctTv text
                correctTv.text = "" + correctCount
                //set incorrectTv text
                incorrectTv.text = "" + incorrectCount
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}