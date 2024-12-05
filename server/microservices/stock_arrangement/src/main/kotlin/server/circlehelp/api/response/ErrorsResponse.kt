package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.http.HttpStatus
import java.util.Collections
import java.util.stream.Stream
import java.util.stream.StreamSupport

data class ErrorsResponse(val errors: Errors, @JsonIgnore val statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY) {

    constructor(body: Iterable<String> = listOf(), statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY)
            : this(Errors(body), statusCode)
    constructor(body: String, statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY)
            : this(Collections.singleton(body), statusCode)

    constructor(throwable: Throwable, statusCode: HttpStatus = HttpStatus.UNPROCESSABLE_ENTITY)
            : this("${throwable.stackTraceToString()}", statusCode)

    fun addAsCopy(errorsResponse: ErrorsResponse?) : ErrorsResponse {

        if (errorsResponse == null) return this

        return ErrorsResponse(Stream.concat(StreamSupport.stream(errors.body.spliterator(), false),
            StreamSupport.stream(errorsResponse.errors.body.spliterator(), false)).toList())
    }

}

