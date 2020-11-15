package com.daveace.salesdiaryrestapi.service

interface SMSService {

    fun send(phone:String, text:String):Boolean
}