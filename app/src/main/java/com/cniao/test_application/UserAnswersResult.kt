package com.cniao.test_application

class UserAnswersResult {
    var question: String
    var option1: String
    var option2: String
    var option3: String
    var option4: String
    var answer: String
    var userAnswer: String? = null

    constructor(
        question: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        answer: String
    ) {
        this.question = question
        this.option1 = option1
        this.option2 = option2
        this.option3 = option3
        this.option4 = option4
        this.answer = answer
    }

    constructor(
        question: String,
        option1: String,
        option2: String,
        option3: String,
        option4: String,
        answer: String,
        userAnswer: String?
    ) {
        this.question = question
        this.option1 = option1
        this.option2 = option2
        this.option3 = option3
        this.option4 = option4
        this.answer = answer
        this.userAnswer = userAnswer
    }
}