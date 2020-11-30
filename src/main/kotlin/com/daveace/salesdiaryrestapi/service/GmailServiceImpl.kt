package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.GmailCredentials
import com.daveace.salesdiaryrestapi.domain.Mail
import com.daveace.salesdiaryrestapi.service.GmailService.Companion.APP_NAME
import com.daveace.salesdiaryrestapi.service.GmailService.Companion.FACTORY
import com.daveace.salesdiaryrestapi.service.GmailService.Companion.SENT
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Service
class GmailServiceImpl : GmailService {

    private lateinit var transport: HttpTransport
    private lateinit var googleCredential:GoogleCredential
    private lateinit var gmailCredentials: GmailCredentials

    @Autowired
    fun initTransport(transport: HttpTransport) {
        this.transport = transport
    }

    @Autowired
    fun initGoogleCredential(googleCredential: GoogleCredential){
        this.googleCredential = googleCredential
    }

    @Autowired
    fun initGmailCredentials(gmailCredentials: GmailCredentials) {
        this.gmailCredentials = gmailCredentials
    }

    override fun sendHTML(mail: Mail): Mono<String> {
        return send(mail, GmailService.HTML).apply { subscribe() }
    }

    override fun sendText(mail: Mail): Mono<String> {
        return send(mail, GmailService.TEXT).apply { subscribe() }
    }

    private fun send(mail: Mail, contentType: String = GmailService.TEXT): Mono<String> {
        val appEmail = gmailCredentials.appEmail
        return createEmail(mail, contentType)
                .flatMap { mimeMessage -> createMessageWithEmail(mimeMessage) }
                .map { message ->
                    createGmail().users()
                            .messages()
                            .send(appEmail, message)
                            .execute()
                            .labelIds
                            .contains(SENT)
                }
                .flatMap { sent ->
                    if (!sent) Mono.just("Failed to send mail")
                    Mono.just("Mail sent")
                }.onErrorMap { error ->
                    throw RuntimeException(error.message)
                }
    }

    private fun createGmail(): Gmail {
        return Gmail.Builder(transport, FACTORY, googleCredential)
                .setApplicationName(APP_NAME)
                .build()
    }

    private fun createEmail(mail: Mail, contentType: String): Mono<MimeMessage> {
        return Mono.just(MimeMessage(Session.getDefaultInstance(Properties(), null)).apply {
            val part: MimeBodyPart = MimeBodyPart()
            val parts: Multipart = MimeMultipart()
            setFrom(InternetAddress(mail.from))
            setSubject(mail.subject)
            addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(mail.to))
            part.setContent(mail.content, contentType)
            parts.addBodyPart(part)
            setContent(parts)
        })
    }

    private fun createMessageWithEmail(message: MimeMessage): Mono<Message> {
        return Mono.just(Message().apply {
            val bos = ByteArrayOutputStream()
            message.writeTo(bos)
            raw = Base64.encodeBase64URLSafeString(bos.toByteArray())
        })
    }

}

