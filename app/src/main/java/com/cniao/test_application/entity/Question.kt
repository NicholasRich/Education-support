package com.cniao.test_application.entity

import com.cniao.test_application.entity.Answer
import java.io.Serializable

data class Question(
    val id: String? = null,
    val activity_id: String,
    val description: String,
) : Serializable