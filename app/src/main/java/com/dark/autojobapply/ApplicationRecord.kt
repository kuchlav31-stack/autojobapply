package com.dark.autojobapply

data class ApplicationRecord(
    val id: String = "",
    val companyName: String = "",
    val jobTitle: String = "",
    val toEmail: String = "",
    val status: String = "Applied",
    val emailStatus: String = "Sent",
    val appliedAt: Long = 0L,
    val deliveredAt: Long = 0L,
    val openedAt: Long = 0L,
    val clickedAt: Long = 0L,
    val messageId: String = "",
    val emailSubject: String = "",
    val emailBody: String = ""
)