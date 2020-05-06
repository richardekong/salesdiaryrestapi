package com.daveace.salesdiaryrestapi.domain

import com.daveace.salesdiaryrestapi.messaging.CONTENT_PROMPT
import com.daveace.salesdiaryrestapi.messaging.RECEIVER_EMAIL_PROMPT
import com.daveace.salesdiaryrestapi.messaging.SENDER_EMAIL_PROMPT
import com.daveace.salesdiaryrestapi.messaging.SUBJECT_PROMPT
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class Mail(
        @field:NotBlank(message= RECEIVER_EMAIL_PROMPT)
        @field:Email(message= com.daveace.salesdiaryrestapi.messaging.EMAIL_VAL_MSG)
        val from:String,
        @field:NotBlank(message = SENDER_EMAIL_PROMPT)
        @field:Email(message= com.daveace.salesdiaryrestapi.messaging.EMAIL_VAL_MSG)
        val to:String,
        @field:NotBlank(message= SUBJECT_PROMPT)
        val subject:String,
        @field:NotBlank(message= CONTENT_PROMPT)
        val content:String) {

    @field:Email(message= com.daveace.salesdiaryrestapi.messaging.EMAIL_VAL_MSG)
    var cC:String = ""
}

