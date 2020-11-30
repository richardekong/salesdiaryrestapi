package com.daveace.salesdiaryrestapi.service

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Call
import com.twilio.type.PhoneNumber
import com.twilio.type.Twiml
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class VoiceServiceImpl : VoiceService {

    @Value("\${twilio.account.sid}")
    private lateinit var sid: String

    @Value("\${twilio.auth.token}")
    private lateinit var token: String

    @Value("\${twilio.account.phone}")
    private lateinit var twilioPhone: String

    override fun send(phone: String, command: String): Boolean {
        Twilio.init(sid, token)
        return Call.creator(
                PhoneNumber(phone),
                PhoneNumber(twilioPhone),
                Twiml("<Response>" +
                        "<Say>Your Two Factor authentication code is:" +
                        "<say-as interpret-as=\"telephone\">$command</say-as>" +
                        "</Say></Response>"))
                .create()
                .run {
                    this.status == Call.Status.COMPLETED
                }
    }

}

