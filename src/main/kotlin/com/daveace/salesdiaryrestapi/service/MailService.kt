package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.domain.Mail
import reactor.core.publisher.Mono

interface MailService {
    fun sendText(mail: Mail): Mono<String>
    fun sendHTML(mail: Mail): Mono<String>
}