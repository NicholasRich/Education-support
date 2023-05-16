package com.cniao.test_application.entity

import java.io.Serializable

data class Answer(
    val id: String? = null,
    val question_id: String? = null,
    val content: String,
    val correct: Boolean
) : Serializable
