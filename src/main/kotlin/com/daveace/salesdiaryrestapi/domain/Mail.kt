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
        var from:String = "",
        @field:NotBlank(message = SENDER_EMAIL_PROMPT)
        @field:Email(message= com.daveace.salesdiaryrestapi.messaging.EMAIL_VAL_MSG)
        var to:String = "",
        @field:NotBlank(message= SUBJECT_PROMPT)
        var subject:String = "",
        @field:NotBlank(message= CONTENT_PROMPT)
        var content:String = "") {

    @field:Email(message= com.daveace.salesdiaryrestapi.messaging.EMAIL_VAL_MSG)
    var cC:String = ""

    constructor(to:String, subject: String):this(){
        this.to = to
        this.subject = subject
    }

}

