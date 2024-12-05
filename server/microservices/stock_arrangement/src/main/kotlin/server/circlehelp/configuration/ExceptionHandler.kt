package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.extern.java.Log
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.api.response.ErrorsResponseException

@Log
@RestControllerAdvice
class ExceptionHandler(mapperBuilder: Jackson2ObjectMapperBuilder) : ResponseEntityExceptionHandler() {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    @ExceptionHandler(ErrorsResponseException::class)
    fun handle(ex: ErrorsResponseException) : ResponseEntity<String> {
        logger.error(ex.message, ex)
        return ResponseEntity.status(ex.errorsResponse.statusCode)
            .body(objectMapper.writeValueAsString(ex.errorsResponse))
    }


    @ExceptionHandler(AccessDeniedException::class)
    fun handle(e: AccessDeniedException): ResponseEntity<String> {
        logger.info(e.message, e)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(objectMapper.writeValueAsString(ErrorResponse(e.message.orEmpty())))
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handle(e: AuthenticationException): ResponseEntity<String> {
        logger.info(e.message, e)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(objectMapper.writeValueAsString(ErrorResponse(e.message.orEmpty())))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handle(e: IllegalArgumentException): ResponseEntity<String> {
        logger.info(e.message, e)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(objectMapper.writeValueAsString(ErrorResponse(e.message.orEmpty())))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handle(e: NoSuchElementException): ResponseEntity<String> {
        logger.info(e.message, e)
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(objectMapper.writeValueAsString(ErrorResponse(e.message.orEmpty())))
    }

    /**
     * Errors that the developer did not expect are handled here and the log level is recorded as
     * error.
     *
     * @param e Exception
     * @return ProblemDetail
     */
    @ExceptionHandler(Exception::class)
    fun handle(e: Exception): ProblemDetail? {
        logger.error(e.message, e)
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Please contact the administrator."
        )
    }
}