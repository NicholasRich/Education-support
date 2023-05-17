package com.cniao.test_application.entity

import java.io.Serializable
import java.util.Date

data class Course(
    val id: String? = null,
    val course_name: String,
    val edu_email: String? = null,
    val add_time: Date = Date(),
) : Serializable
