package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.api.response.ErrorResponseException
import java.util.concurrent.Callable

/**
 * For using service functions with endpoints.
 */
@Service
class ResponseBodyWriter(mapperBuilder: Jackson2ObjectMapperBuilder) {

    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    fun error(errorResponse: ErrorResponse) : ResponseEntity<String> {
        return ResponseEntity
            .status(errorResponse.statusCode)
            .body(objectMapper.writeValueAsString(errorResponse))
    }

    /**
     * Return a response entity with:
     * - Http status 200 (OK)
     * - obj (as JSON serialized object if obj was not a string).
     * Reason: objectMapper would escape special characters.
     */
    fun body(obj: Any?) : ResponseEntity<String> {
        return ResponseEntity.ok(
            obj.let {
                if (obj is String)
                    it as String
                else
                    objectMapper.writeValueAsString(it)
            }
        )
    }

    /**
     * Wraps the call with either body() or error().
     */
    fun wrap(func: Callable<Any>) : ResponseEntity<String> {
        return try {
            body(func.call())
        } catch (ex: ErrorResponseException) {
            error(ex.errorResponse)
        }
    }
}