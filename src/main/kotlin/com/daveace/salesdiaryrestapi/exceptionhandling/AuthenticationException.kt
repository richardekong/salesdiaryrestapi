package com.daveace.salesdiaryrestapi.exceptionhandling

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException

class AuthenticationException(message: String = HttpStatus.UNAUTHORIZED.reasonPhrase) : AuthenticationException(message)

