package server.server.login.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import server.server.login.api.response.ErrorResponse

@Slf4j
@RestControllerAdvice
class ExceptionHandler(mapperBuilder: Jackson2ObjectMapperBuilder) : ResponseEntityExceptionHandler() {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()

    @ExceptionHandler(AccessDeniedException::class)
    fun handle(e: AccessDeniedException): ResponseEntity<String> {
        logger.info(e.message, e)

        val errorResponse = ErrorResponse("Access Denied.\n${e.message}" ?: "")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(objectMapper.writeValueAsString(errorResponse))
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handle(e: AuthenticationException): ResponseEntity<String> {
        logger.info(e.message, e)

        val errorResponse = ErrorResponse("Authentication failed.\n${e.message ?: ""}")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(objectMapper.writeValueAsString(errorResponse))
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handle(e: UsernameNotFoundException): ResponseEntity<String> {
        logger.info(e.message, e)
        val errorResponse = ErrorResponse("Username not found.\n${e.message ?: ""}")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(objectMapper.writeValueAsString(errorResponse))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handle(e: BadCredentialsException): ResponseEntity<String> {
        logger.info(e.message, e)
        val errorResponse = ErrorResponse("Invalid username or password.\n${e.message ?: ""}")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(objectMapper.writeValueAsString(errorResponse))
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