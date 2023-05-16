package com.cniao.test_application.ui.Repository.Model

data class Project(
    var name: String = "",
    var manager: String = "",
    var members: List<String> = ArrayList(),
    var description: String = "",
    var startTime: String = "",
    var deadline: String = "",
    var taskCount: Int = 0,
    var completeCount: Int = 0,
    var status: String = ""
)