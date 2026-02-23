package io.hhplus.tdd

import io.hhplus.tdd.common.error.CommonErrorResponses
import io.hhplus.tdd.common.error.ErrorResponse
import io.hhplus.tdd.common.error.PointBadRequestException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ApiControllerAdvice : ResponseEntityExceptionHandler() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(PointBadRequestException::class)
    fun handlePointBadRequestException(e: PointBadRequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            e.errorResponse,
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            CommonErrorResponses.INTERNAL_SERVER_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR,
        )
    }
}
