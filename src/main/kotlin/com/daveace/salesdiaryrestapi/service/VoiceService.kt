package com.daveace.salesdiaryrestapi.service

interface VoiceService {

    fun send(phone:String, command:String): Boolean
}