package com.cniao.test_application

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultAdapter(//items array list
    private val userAnswersResult: List<UserAnswersResult>, //context
    private val context: Context
) : RecyclerView.Adapter<ResultAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_adapter_layout, null)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        //get single item details from list
        val result = userAnswersResult[position]
        //set item detail to tv
        holder.questionTv.text = result.question
        holder.option1Tv.text = result.option1
        holder.option2Tv.text = result.option2
        holder.option3Tv.text = result.option3
        holder.option4Tv.text = result.option4
        holder.answerTv.text = result.answer
        holder.userAnswerTv.text = result.userAnswer
    }

    override fun getItemCount(): Int {
        return userAnswersResult.size
    }

    //MyViewHolder class to hold biew reference for every item in the RecyclerView
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTv: TextView
        val option1Tv: TextView
        val option2Tv: TextView
        val option3Tv: TextView
        val option4Tv: TextView
        val answerTv: TextView
        val userAnswerTv: TextView

        init {

            //get tv from recycler_adapter_layout xml file
            questionTv = itemView.findViewById(R.id.questionTv)
            option1Tv = itemView.findViewById(R.id.option1Tv)
            option2Tv = itemView.findViewById(R.id.option2Tv)
            option3Tv = itemView.findViewById(R.id.option3Tv)
            option4Tv = itemView.findViewById(R.id.option4Tv)
            answerTv = itemView.findViewById(R.id.answerTv)
            userAnswerTv = itemView.findViewById(R.id.userAnswerTv)
        }
    }
}