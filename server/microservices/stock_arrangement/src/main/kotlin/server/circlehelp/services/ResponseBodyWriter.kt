package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.api.response.ErrorsResponse
import server.circlehelp.api.response.ErrorsResponseException
import java.util.concurrent.Callable

/**
 * For using service functions with endpoints.
 */
@Service
class ResponseBodyWriter(mapperBuilder: Jackson2ObjectMapperBuilder) {

    private val logger = LoggerFactory.getLogger(ResponseBodyWriter::class.java)
    private val objectMapper = mapperBuilder.build<ObjectMapper>()

    fun error(errorsResponse: ErrorsResponse) : ResponseEntity<String> {
        logger.error(errorsResponse.toString())
        return ResponseEntity
            .status(errorsResponse.statusCode)
            .body(objectMapper.writeValueAsString(errorsResponse))
    }

    /**
     * Return a response entity with:
     * - Http status 200 (OK)
     * - [obj] (as JSON serialized object if [obj] was not a string).
     * Reason: [objectMapper] would escape special characters.
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
     * Wraps the call with either [body] () or [error] ().
     */
    fun wrap(func: Callable<Any>) : ResponseEntity<String> {
        return try {
            body(func.call())
        } catch (ex: ErrorsResponseException) {
            error(ex.errorsResponse)
        }
    }
}