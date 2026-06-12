package com.findhubtracker.data.model

data class EmailConfig(
    val recipientEmail: String = "",
    val smtpServer: String = "smtp.gmail.com",
    val smtpPort: String = "587",
    val username: String = "",
    val password: String = "",
    val isEnabled: Boolean = false
)
