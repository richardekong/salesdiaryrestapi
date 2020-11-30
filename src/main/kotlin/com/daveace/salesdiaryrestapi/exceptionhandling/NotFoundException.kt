package com.daveace.salesdiaryrestapi.exceptionhandling

import org.springframework.http.HttpStatus

class NotFoundException(override val message: String = HttpStatus.NOT_FOUND.reasonPhrase) : RuntimeException(message)
