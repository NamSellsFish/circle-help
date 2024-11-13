package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.ShelfAtomicOpsService

const val shelf = "/shelves"

@Controller
@RequestMapping("$baseURL$shelf")
class ShelvesController(
    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val shelfAtomicOpsService: ShelfAtomicOpsService,
    private val responseBodyWriter: ResponseBodyWriter
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(ShelvesController::class.java)


}