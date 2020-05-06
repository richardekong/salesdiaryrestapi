package com.daveace.salesdiaryrestapi.exceptionhandling

class ErrorResponse<T>() {

    var status: Int = 0
    lateinit var message:String
    var timeStamp:Long = 0


}

