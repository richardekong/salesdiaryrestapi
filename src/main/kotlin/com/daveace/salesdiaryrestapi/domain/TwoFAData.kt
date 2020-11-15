package com.daveace.salesdiaryrestapi.domain

import java.time.LocalDateTime

data class TwoFAData(
        var enabled: Boolean = false,
        var code: String = "",
        var validity: LocalDateTime = LocalDateTime.now().plusMinutes(TIME_FRAME),
        var channel: String = Channel.EMAIL.channel) {

    companion object {
        val TIME_FRAME = TimeFrame.MEDIUM.timeFrame
    }

    constructor(enabled: Boolean, code: String) : this() {
        this.enabled = enabled
        this.code = code
    }

    constructor(enabled: Boolean, code: String, channel: String) : this() {
        this.enabled = enabled
        this.code = code
        this.channel = channel
    }

    fun isExpired():Boolean{
        return LocalDateTime.now().isAfter(validity)
    }

    enum class Channel(val channel: String) {
        EMAIL("email"),
        SMS("sms"),
        VOICE("voice");

        companion object {
            fun channels(): Array<String> {
                return arrayOf(EMAIL.channel, SMS.channel, VOICE.channel)
            }
        }
    }

    enum class TimeFrame(val timeFrame:Long){
        SHORT(2L),
        MEDIUM(5L),
        LONG(10L);
    }
}

