package com.cniao.test_application

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.cniao.test_application.entity.Answer
import com.cniao.test_application.entity.Question
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class QuestionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        val id = intent.getStringExtra("activity_id").toString()
        val activityName = intent.getStringExtra("activity_name").toString()
        val availableTime = intent.getStringExtra("available_time").toString()
        val number = intent.getStringExtra("number").toString()
        val questionId = intent.getStringExtra("question_id").toString()
        if (questionId == "null") {
//            findViewById<FloatingActionButton>(R.id.addAnswer)
//                .setOnClickListener {
//                    addAnswerLayout(null)
//                }
            findViewById<Button>(R.id.submit).setOnClickListener {
                if (validate()) {
                    submit(id, activityName, availableTime, number)
                }
            }
        } else {
//            val layout2 = findViewById<LinearLayout>(R.id.linearLayout2)
//            val layout3 = findViewById<LinearLayout>(R.id.linearLayout3)
//            val button1 = findViewById<FloatingActionButton>(R.id.addAnswer)
            val button2 = findViewById<Button>(R.id.submit)
//            (layout2.parent as ViewGroup).removeView(layout2)
//            (layout3.parent as ViewGroup).removeView(layout3)
//            button1.isVisible = false
            button2.isVisible = false
            findQuestionById(questionId)
            findAnswerByQuestionId(questionId)
        }
        supportActionBar?.title = "Add Question"

    }

    private fun dp(number: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            number,
            resources.displayMetrics
        ).toInt()
    }

    private fun validate(): Boolean {
        val layout = findViewById<LinearLayout>(R.id.linearLayout)
        var submit = true
        var checked = false
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i) as LinearLayout
            var checkbox = CheckBox(this)
            var editText: EditText
            if (i != 0) {
                checkbox = child.getChildAt(0) as CheckBox
                editText = child.getChildAt(1) as EditText
            } else {
                editText = child.getChildAt(0) as EditText
            }
            if (editText.length() == 0) {
                editText.error = "This field is required!!!"
                submit = false
            }
            if (checkbox.isChecked) {
                checked = true
            }
        }
        if (!checked) {
            AlertDialog.Builder(this).setMessage("At least 1 correct answer is required!!!")
                .setCancelable(false)
                .setPositiveButton("Close") { _, _ -> }
                .create().show()
        }
        return submit && checked
    }

    private fun submit(id: String, activityName: String, availableTime: String, number: String) {
        val layout = findViewById<LinearLayout>(R.id.linearLayout)
        var questionDesc = ""
        val answerList = mutableListOf<Answer>()
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i) as LinearLayout
            if (i == 0) {
                val text = (child.getChildAt(0) as EditText).text
                questionDesc += text
            } else {
                val checkbox = child.getChildAt(0) as CheckBox
                val editText = child.getChildAt(1) as EditText
                answerList.add(Answer(null, id, editText.text.toString(), checkbox.isChecked))
            }
        }
        val question = Question(null, id, questionDesc, number)
        val db = Firebase.firestore
        db.collection("Question").add(question)
            .addOnSuccessListener {
                val intent = Intent(this, ActivityDetail::class.java)
                intent.putExtra("question_id", it.id)
                intent.putExtra("activity_id", id)
                intent.putExtra("description", questionDesc)
                intent.putExtra("activity_name", activityName)
                intent.putExtra("available_time", availableTime)
                addAnswer(it.id, answerList, intent)
                packageActivity(it.id, questionDesc, id, answerList)
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun packageActivity(
        question_id: String,
        description: String,
        activityId: String,
        answerList: List<Answer>
    ) {
        val db = Firebase.firestore
//        for (answer in answerList) {
        val ref = db.collection("Activity")
            .document(activityId)
            .collection("questions")
            .document(question_id)
        var correct = ""
        var j = 1
        for (answer in answerList) {
            if (answer.correct) {
                correct += "${j++},"
            }
        }
        correct = correct.substring(0, correct.length - 1)
        val data = hashMapOf(
            "answer" to correct,
            "option1" to answerList[0].content,
            "option2" to answerList[1].content,
            "option3" to answerList[2].content,
            "option4" to answerList[3].content,
            "question" to description,
        )
//        }

        ref.set(data as Map<String, Any>, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    ContentValues.TAG,
                    "DocumentSnapshot successfully written!"
                )
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
            }
    }

    private fun addAnswer(id: String, list: List<Answer>, intent: Intent) {
        val answerList = mutableListOf<Answer>()
        for (answer in list) {
            answerList.add(Answer(null, id, answer.content, answer.correct))
        }
        val db = Firebase.firestore
        db.runBatch {
            for (answer in answerList) {
                val collection = db.collection("Answer").document()
                it.set(collection, answer)
            }
        }.addOnCompleteListener {
            finish()
            startActivity(intent)
        }.addOnFailureListener { e ->
            Log.w(ContentValues.TAG, "Error adding document", e)
        }
    }

    private fun addAnswerLayout(answer: Answer?) {
        val view = findViewById<LinearLayout>(R.id.linearLayout)
        val layout = LinearLayout(this)
        val layoutLp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutLp.topMargin = dp(30f)
        layout.layoutParams = layoutLp
        val editText = EditText(this)
        val editTextLp = LinearLayout.LayoutParams(
            dp(250f),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editText.hint = "Please input answer..."
        editText.layoutParams = editTextLp
        val checkbox = CheckBox(this)
        val checkboxLp = LinearLayout.LayoutParams(
            dp(40f),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        checkbox.layoutParams = checkboxLp
        val button = Button(this)
        button.text = "X"
        button.setOnClickListener {
            (it.parent.parent as ViewGroup).removeView(it.parent as ViewGroup)
        }
        if (answer == null) {
//            layout.addView(button)
        } else {
            editText.isEnabled = false
            editText.setText(answer.content)
            editText.setTextColor(Color.BLACK)
            checkbox.isChecked = answer.correct
            checkbox.isEnabled = false
        }
        layout.addView(checkbox)
        layout.addView(editText)
        view.addView(layout)
    }

    private fun renderAnswer(id: Int, answer: Answer) {
        val layout = findViewById<LinearLayout>(id)
        val checkbox = layout.getChildAt(0) as CheckBox
        val editText = layout.getChildAt(1) as EditText
        editText.isEnabled = false
        editText.setText(answer.content)
        editText.setTextColor(Color.BLACK)
        checkbox.isChecked = answer.correct
        checkbox.isEnabled = false
    }

    private fun findQuestionById(id: String) {
        val db = Firebase.firestore
        db.collection("Question").document(id)
            .get()
            .addOnSuccessListener { result ->
                val data = result.data as HashMap
                val editText = findViewById<EditText>(R.id.description)
                editText.hint = ""
                editText.isClickable = false
                editText.isEnabled = false
                editText.setTextColor(Color.BLACK)
                editText.setText(data["description"].toString())
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun findAnswerByQuestionId(id: String) {
        val db = Firebase.firestore
        db.collection("Answer")
            .whereEqualTo("question_id", id).get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Answer>()
                for (document in result) {
                    val data = document.data
                    list.add(
                        Answer(
                            null,
                            id,
                            data["content"].toString(),
                            data["correct"] as Boolean
                        )
                    )

//                    addAnswerLayout(
//                        Answer(
//                            null,
//                            id,
//                            data["content"].toString(),
//                            data["correct"] as Boolean
//                        )
//                    )
                }
                renderAnswer(R.id.linearLayout2, list[0])
                renderAnswer(R.id.linearLayout3, list[1])
                renderAnswer(R.id.linearLayout4, list[2])
                renderAnswer(R.id.linearLayout5, list[3])
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }
}