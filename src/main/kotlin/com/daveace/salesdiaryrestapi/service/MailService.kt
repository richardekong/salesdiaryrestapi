package com.daveace.salesdiaryrestapi.service

import com.daveace.salesdiaryrestapi.messaging.Mail

interface MailService {
    fun sendText(mail:Mail)
    fun sendHTML(mail:Mail)
}