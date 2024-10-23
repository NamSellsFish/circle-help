package server.circlehelp.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.api.response.ErrorResponse

@Service
class ResponseBodyWriter(@Autowired private val mapperBuilder: Jackson2ObjectMapperBuilder) {

    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    fun toResponseEntity(errorResponse: ErrorResponse) : ResponseEntity<String> {
        return ResponseEntity
            .status(errorResponse.statusCode)
            .body(objectMapper.writeValueAsString(errorResponse))
    }
}