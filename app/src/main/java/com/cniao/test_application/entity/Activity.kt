package com.cniao.test_application.entity

import java.io.Serializable
import java.util.Date

data class Activity(
    val id: String? = null,
    val course_id: String? = null,
    val activity_name: String,
    val available_time: Date? = null,
) : Serializable