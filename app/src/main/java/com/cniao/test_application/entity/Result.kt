package com.cniao.test_application.entity

import java.util.Date

data class Result(
    val activity_id: String,
    val activity_name: String,
    val email: String,
    val available_time: Date
)
