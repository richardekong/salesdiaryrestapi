package com.daveace.salesdiaryrestapi.exceptionhandling

class RestException(override val message:String) : RuntimeException(message)
