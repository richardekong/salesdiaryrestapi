package com.daveace.salesdiaryrestapi.messaging

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class Mail(
        @field:NotBlank(message= RECEIVER_EMAIL_PROMPT)
        @field:Email(message= EMAIL_VAL_MSG)
        val from:String,
        @field:NotBlank(message = SENDER_EMAIL_PROMPT)
        @field:Email(message= EMAIL_VAL_MSG)
        val to:String,
        @field:NotBlank(message= SUBJECT_PROMPT)
        val subject:String,
        @field:NotBlank(message= CONTENT_PROMPT)
        val content:String) {

    @field:Email(message= EMAIL_VAL_MSG)
    var cC:String = ""
}

