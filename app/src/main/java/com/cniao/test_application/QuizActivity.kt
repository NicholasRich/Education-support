package com.cniao.test_application

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class QuizActivity : AppCompatActivity() {
    private var userEmail: String? = null
    private var activityName: String? = null

    //declare options RelativeLayout
    private var option1Layout: RelativeLayout? = null
    private var option2Layout: RelativeLayout? = null
    private var option3Layout: RelativeLayout? = null
    private var option4Layout: RelativeLayout? = null

    //declare options TextView
    private var option1Tv: TextView? = null
    private var option2Tv: TextView? = null
    private var option3Tv: TextView? = null
    private var option4Tv: TextView? = null

    //declare options ImageView
    private var option1Icon: ImageView? = null
    private var option2Icon: ImageView? = null
    private var option3Icon: ImageView? = null
    private var option4Icon: ImageView? = null

    //declare question TextView
    private var questionTv: TextView? = null

    //declare current question TextView
    private var currentQuestionTv: TextView? = null

    //declare total questions TextView
    private var totalQuestionsTv: TextView? = null
    private var nextQuestionBtn: AppCompatButton? = null

    //create questions List
    private val questionsLists: MutableList<QuetionsList> = ArrayList()

    //init current question position
    private var currentQuestionPosition = 0

    //init selected option number 0:no option is selected;
    private val selectedOptionList: MutableList<String> = ArrayList()
    private var answersCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        supportActionBar?.title = "Quiz"

        userEmail = intent.getStringExtra("userEmail")
//        if (userEmail!!.contains("@")) {
//            userEmail = userEmail!!.substring(0, userEmail!!.indexOf("@"))
//        }
        activityName = intent.getStringExtra("activityName")

        //init elements
        initElements()


        val db = Firebase.firestore
        db.collection("Activity").document(activityName!!).collection("questions")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val questionDesc = document.data["question"]
                    val getOption1 = document.data["option1"]
                    val getOption2 = document.data["option2"]
                    val getOption3 = document.data["option3"]
                    val getOption4 = document.data["option4"]
                    val getAnswer = document.data["answer"] as? String

                    val answerList = getAnswer?.split(",")?.map { it.trim() }

                    //create question list object and add details
                    val questionsList = answerList?.let {
                        QuetionsList(
                            questionDesc!! as String,
                            getOption1!! as String,
                            getOption2!! as String,
                            getOption3!! as String,
                            getOption4!! as String,
                            it
                        )
                    }
                    //add to list
                    if (questionsList != null) {
                        questionsLists.add(questionsList)
                    }

                }
                totalQuestionsTv!!.text = "/" + questionsLists.size

                //select first question by default
                selectQuestion(currentQuestionPosition)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        option1Layout!!.setOnClickListener {
            //if only one answer clear previous answer
            if (answersCount == 1) {
                selectedOptionList.clear()
                selectedOptionList.add("1")
                selectOption(option1Layout, option1Icon)
            } else {
                //check if this option is selected
                if (selectedOptionList.contains("1")) {
                    //delete 1 from list and change style
                    selectedOptionList.remove("1")
                    removeOption(option1Layout, option1Icon)
                } else {
                    //add value 1 to list
                    selectedOptionList.add("1")
                    selectOption(option1Layout, option1Icon)
                }
            }
        }

        option2Layout!!.setOnClickListener {
            //if only one answer clear previous answer
            if (answersCount == 1) {
                selectedOptionList.clear()
                selectedOptionList.add("2")
                selectOption(option2Layout, option2Icon)
            } else {
                //check if this option is selected
                if (selectedOptionList.contains("2")) {
                    //delete 2 from list and change style
                    selectedOptionList.remove("2")
                    removeOption(option2Layout, option2Icon)
                } else {
                    //add value 2 to list
                    selectedOptionList.add("2")
                    selectOption(option2Layout, option2Icon)
                }
            }
        }

        option3Layout!!.setOnClickListener {
            //if only one answer clear previous answer
            if (answersCount == 1) {
                selectedOptionList.clear()
                selectedOptionList.add("3")
                selectOption(option3Layout, option3Icon)
            } else {
                //check if this option is selected
                if (selectedOptionList.contains("3")) {
                    //delete 3 from list and change style
                    selectedOptionList.remove("3")
                    removeOption(option3Layout, option3Icon)
                } else {
                    //add value 3 to list
                    selectedOptionList.add("3")
                    selectOption(option3Layout, option3Icon)
                }
            }
        }

        option4Layout!!.setOnClickListener {
            //if only one answer clear previous answer
            if (answersCount == 1) {
                selectedOptionList.clear()
                selectedOptionList.add("4")
                selectOption(option4Layout, option4Icon)
            } else {
                //check if this option is selected
                if (selectedOptionList.contains("4")) {
                    //delete 4 from list and change style
                    selectedOptionList.remove("4")
                    removeOption(option4Layout, option4Icon)
                } else {
                    //add value 4 to list
                    selectedOptionList.add("4")
                    selectOption(option4Layout, option4Icon)
                }
            }
        }

        nextQuestionBtn!!.setOnClickListener {
            //check whether user select option
            if (selectedOptionList.size == 0) {
                Toast.makeText(this@QuizActivity, "Please select an option", Toast.LENGTH_SHORT)
                    .show()
            } else {
                //set user selected answer and save to firebase
                val questionIndex = currentQuestionPosition + 1
                val questionNum = "question$questionIndex"
                val userSelectedStr = selectedOptionList.sorted().joinToString(",")
                //realtime rf
//                resultReference.child(activity).child(userEmail!!).child(questionNum)
//                    .setValue(userSelectedStr)

                val resultRef =
                    db.collection("Result").document(activityName!!).collection(userEmail!!)
                        .document("answer")

                val data = hashMapOf(
                    questionNum to userSelectedStr
                )

                resultRef.set(data as Map<String, Any>, SetOptions.merge())
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }


                //reset selected option list to default empty
                selectedOptionList.clear()

                //add currentQuestionPosition value
                currentQuestionPosition = currentQuestionPosition + 1

                //check if question list has more questions
                if (currentQuestionPosition < questionsLists.size) {
                    selectQuestion(currentQuestionPosition)
                } else {
                    finishQuiz()
                }
            }
        }
    }

    private fun initElements() {
        option1Layout = findViewById(R.id.option1Layout)
        option2Layout = findViewById(R.id.option2Layout)
        option3Layout = findViewById(R.id.option3Layout)
        option4Layout = findViewById(R.id.option4Layout)
        option1Tv = findViewById(R.id.option1Tv)
        option2Tv = findViewById(R.id.option2Tv)
        option3Tv = findViewById(R.id.option3Tv)
        option4Tv = findViewById(R.id.option4Tv)
        option1Icon = findViewById(R.id.option1Icon)
        option2Icon = findViewById(R.id.option2Icon)
        option3Icon = findViewById(R.id.option3Icon)
        option4Icon = findViewById(R.id.option4Icon)
        questionTv = findViewById(R.id.questionTv)
        currentQuestionTv = findViewById(R.id.currentQuestionTv)
        totalQuestionsTv = findViewById(R.id.totalQuestionsTv)
        nextQuestionBtn = findViewById(R.id.nextQuestionBtn)
    }

    private fun finishQuiz() {
        val intent = Intent(this@QuizActivity, QuizResultActivity::class.java)
        intent.putExtra("userEmail", userEmail)
        intent.putExtra("activityName", activityName)
        startActivity(intent)
    }

    private fun selectQuestion(quesitonListPosition: Int) {
        answersCount = questionsLists[currentQuestionPosition].answer.size
        //reset options for next question
        resetOptions()
        //get question details and set to tv
        questionTv!!.text = questionsLists[quesitonListPosition].question
        option1Tv!!.text = questionsLists[quesitonListPosition].option1
        option2Tv!!.text = questionsLists[quesitonListPosition].option2
        option3Tv!!.text = questionsLists[quesitonListPosition].option3
        option4Tv!!.text = questionsLists[quesitonListPosition].option4
        //set current question number to currentQuestionTv
        currentQuestionTv!!.text = "Question" + (quesitonListPosition + 1)
        if (currentQuestionPosition + 1 == questionsLists.size) {
            nextQuestionBtn!!.text = "Submit"
        }
    }

    /**
     * reset option function
     */
    private fun resetOptions() {
        option1Layout!!.setBackgroundResource(R.drawable.option_bg)
        option2Layout!!.setBackgroundResource(R.drawable.option_bg)
        option3Layout!!.setBackgroundResource(R.drawable.option_bg)
        option4Layout!!.setBackgroundResource(R.drawable.option_bg)
        option1Icon!!.setImageResource(R.drawable.option_bg)
        option2Icon!!.setImageResource(R.drawable.option_bg)
        option3Icon!!.setImageResource(R.drawable.option_bg)
        option4Icon!!.setImageResource(R.drawable.option_bg)
    }

    private fun selectOption(
        selectedOptionLayout: RelativeLayout?,
        selectedOptionIcon: ImageView?
    ) {
        val answerSize = questionsLists[currentQuestionPosition].answer.size
        //if one answer set resource
        if (answersCount == 1) {
            resetOptions()
        }
        selectedOptionLayout!!.setBackgroundResource(R.drawable.option_selected_bg)
        selectedOptionIcon!!.setImageResource(R.drawable.option_selected_icon)
    }

    private fun removeOption(
        selectedOptionLayout: RelativeLayout?,
        selectedOptionIcon: ImageView?
    ) {
        selectedOptionLayout!!.setBackgroundResource(R.drawable.option_bg)
        selectedOptionIcon!!.setImageResource(R.drawable.option_bg)
    }
}