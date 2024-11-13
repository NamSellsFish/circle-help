package server.circlehelp.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.MoveProductToShelfRequest
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductList
import server.circlehelp.api.response.SwapRequest
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.ShelfService

const val shelf = "/shelves"

@Controller
@RequestMapping("$baseURL$shelf")
class ShelvesController(
    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val shelfService: ShelfService,
    private val responseBodyWriter: ResponseBodyWriter
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(ShelvesController::class.java)


}