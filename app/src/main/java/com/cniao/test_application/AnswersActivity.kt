package com.cniao.test_application

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.TreeMap

class AnswersActivity : AppCompatActivity() {
    private var userEmail: String? = null
    private var activityName: String? = null
    private var userAnswersResultList: MutableList<UserAnswersResult>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answers)
        supportActionBar?.title = "Result Detail"
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        //Demo1.get from xml file
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        //set recyclerview size fixed for every item in the recyclerview
        recyclerView.setHasFixedSize(true)
        //set layout manager to recyclerview ex:LinerLayoutManager(vertical mode)
        recyclerView.layoutManager = LinearLayoutManager(this@AnswersActivity)

        //get parameter from quizactivity
        userEmail = intent.getStringExtra("userEmail")
        activityName = intent.getStringExtra("activityName")
        userAnswersResultList = ArrayList()

        val db = Firebase.firestore
        //get answerList
        db.collection("Activity").document(activityName!!).collection("questions")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val questionDesc = document.data["question"] as? String
                    val option1 = document.data["option1"] as? String
                    val option2 = document.data["option2"] as? String
                    val option3 = document.data["option3"] as? String
                    val option4 = document.data["option4"] as? String
                    val answer = document.data["answer"] as? String

                    val userAnswersResult = UserAnswersResult(
                        questionDesc!!,
                        option1!!,
                        option2!!,
                        option3!!,
                        option4!!,
                        answer!!
                    )
                    (userAnswersResultList as ArrayList<UserAnswersResult>).add(userAnswersResult)
                }
                var userAnswersList = mutableListOf<String>()
                subSelect(userAnswersList, recyclerView)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }


    }

    fun subSelect(userAnswersList: MutableList<String>, recyclerView: RecyclerView) {
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
                            val value = entry.value
                            userAnswersList.add(value.toString())
                        }
                    }
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
                //add user answer to userAnswersResultList
                for (i in 0 until userAnswersList.size) {
                    (userAnswersResultList as ArrayList<UserAnswersResult>).get(i).userAnswer =
                        userAnswersList[i]
                }
                //3.set adapter to RecyclerView
                recyclerView.adapter = ResultAdapter(
                    userAnswersResultList as ArrayList<UserAnswersResult>,
                    this@AnswersActivity
                )
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}