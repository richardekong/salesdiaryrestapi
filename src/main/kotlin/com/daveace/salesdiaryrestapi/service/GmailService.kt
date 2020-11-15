package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import com.google.api.client.json.jackson2.JacksonFactory
import reactor.core.publisher.Mono

interface GmailService {

    companion object{
        const val APP_NAME = "salesdiaryrestapi"
        const val SENT = "SENT"
        const val TEXT = "text/plain"
        const val HTML = "text/html;charset=utf-8"
        val FACTORY = JacksonFactory.getDefaultInstance()!!
    }
    fun sendText(mail:Mail):Mono<String>
    fun sendHTML(mail: Mail):Mono<String>
}