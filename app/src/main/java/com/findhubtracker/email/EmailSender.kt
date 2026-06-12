package com.findhubtracker.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {

    private const val TAG = "EmailSender"

    suspend fun sendEmail(
        recipient: String,
        subject: String,
        body: String,
        smtpServer: String,
        smtpPort: String,
        username: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", smtpServer)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.ssl.trust", smtpServer)
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                this.subject = subject
                setText(body)
            }

            Transport.send(message)
            Log.d(TAG, "Email sent to $recipient")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email", e)
            throw e
        }
    }
}
