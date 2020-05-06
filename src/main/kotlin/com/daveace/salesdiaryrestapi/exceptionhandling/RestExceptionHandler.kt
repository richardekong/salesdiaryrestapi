package com.daveace.salesdiaryrestapi.exceptionhandling

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import reactor.core.publisher.Mono

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler
    fun <T> handleException(ex: Exception): ResponseEntity<ErrorResponse<T>>? {
        val status: HttpStatus = HttpStatus.BAD_REQUEST
        val errorResponse: ErrorResponse<T> = ErrorResponse()
        return ex.message?.let { createErrorResponse(errorResponse, status, it) }
    }

    @ExceptionHandler
    fun <T>handleException(ex: RestException): ResponseEntity<ErrorResponse<T>>? {
        val status: HttpStatus = HttpStatus.NOT_FOUND
        val errorResponse: ErrorResponse<T> = ErrorResponse()
        return createErrorResponse(errorResponse, status, ex.message)
    }

    @ExceptionHandler
    fun <T> handleException(ex: AuthenticationException): ResponseEntity<ErrorResponse<T>>? {
        val status: HttpStatus = HttpStatus.UNAUTHORIZED
        val errorResponse: ErrorResponse<T> = ErrorResponse()
        return ex.message?.let { createErrorResponse(errorResponse, status, it) }
    }

    @ExceptionHandler
    fun <T> handleException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse<T>> {
        val status: HttpStatus = HttpStatus.BAD_REQUEST
        val errorResponse: ErrorResponse<T> = ErrorResponse()
        val errorMessageBuilder: StringBuilder = StringBuilder()
        val errors: List<ObjectError> = ex.bindingResult.allErrors
        errors.forEach {
            val errorIndex: Int = errors.indexOf(it)
            val lastIndex: Int = errors.size - 1
            val defaultErrorMessage: String? = it.defaultMessage
            if (errorIndex < lastIndex)
                errorMessageBuilder.append(defaultErrorMessage.plus(",\u0020and\u0020"))
            else errorMessageBuilder.append(defaultErrorMessage)
        }
        return createErrorResponse(errorResponse, status, errorMessageBuilder.toString())
    }

    fun <T> createErrorResponse(error: ErrorResponse<T>, status: HttpStatus, message: String)
            : ResponseEntity<ErrorResponse<T>> {
        error.status = status.value()
        error.message = message
        error.timeStamp = System.currentTimeMillis()
        return ResponseEntity(error, status)
    }
}

