package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.extern.java.Log
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import server.circlehelp.api.response.ErrorResponseException

@Log
@RestControllerAdvice
class ExceptionHandler(mapperBuilder: Jackson2ObjectMapperBuilder) : ResponseEntityExceptionHandler() {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    @ExceptionHandler(ErrorResponseException::class)
    fun handle(ex: ErrorResponseException) : ResponseEntity<String> {
        logger.error(ex.message, ex)
        return ResponseEntity.status(ex.errorResponse.statusCode)
            .body(objectMapper.writeValueAsString(ex.errorResponse))
    }


    @ExceptionHandler(AccessDeniedException::class)
    fun handle(e: AccessDeniedException): ProblemDetail? {
        logger.info(e.message, e)
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handle(e: AuthenticationException): ProblemDetail? {
        logger.info(e.message, e)
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
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