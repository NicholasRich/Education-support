package com.cniao.test_application

class QuetionsList(
    var question: String,
    var option1: String,
    var option2: String,
    var option3: String,
    var option4: String,
    var answer: List<String>
) {
    var userSelectedAnswer: List<String>? = null

}