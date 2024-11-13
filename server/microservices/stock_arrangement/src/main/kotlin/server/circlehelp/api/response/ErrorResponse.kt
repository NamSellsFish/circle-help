package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import java.util.Collections
import java.util.stream.Stream
import java.util.stream.StreamSupport

data class ErrorResponse(val errors: Errors, @JsonIgnore val statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY) {

    constructor(body: Iterable<String> = listOf(), statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY)
            : this(Errors(body), statusCode)
    constructor(body: String, statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY)
            : this(Collections.singleton(body), statusCode)

    fun addAsCopy(errorResponse: ErrorResponse?) : ErrorResponse {

        if (errorResponse == null) return this

        return ErrorResponse(Stream.concat(StreamSupport.stream(errors.body.spliterator(), false),
            StreamSupport.stream(errorResponse.errors.body.spliterator(), false)).toList())
    }

}

