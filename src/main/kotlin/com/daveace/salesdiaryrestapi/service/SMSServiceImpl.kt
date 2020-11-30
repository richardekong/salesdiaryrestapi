package com.daveace.salesdiaryrestapi.service

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service

@Service
@PropertySource("classpath:twilio.config.properties")
class SMSServiceImpl : SMSService {

    @Value("\${twilio.account.sid}")
    private lateinit var sid: String

    @Value("\${twilio.auth.token}")
    private lateinit var token: String

    @Value("\${twilio.account.phone}")
    private lateinit var twilioPhone: String

    @Value("\${twilio.message.body}")
    private lateinit var body: String

    override fun send(phone: String, text: String): Boolean {
        Twilio.init(sid, token)
        return Message
                .creator(
                        PhoneNumber(phone),
                        PhoneNumber(twilioPhone),
                        "\n$body\u0020$text")
                .create().run {
                    status == Message.Status.SENT
                }

    }
}