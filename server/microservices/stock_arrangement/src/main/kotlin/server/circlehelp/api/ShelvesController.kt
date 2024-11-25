package server.circlehelp.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.MoveProductToShelfRequest
import server.circlehelp.api.response.ProductDetails
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductList
import server.circlehelp.api.response.ProductOnCompartmentDto
import server.circlehelp.api.response.SwapRequest
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.ShelfService
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

const val shelf = "/shelves"

@RestController
@RequestMapping("$baseURL$shelf")
@ResponseStatus(HttpStatus.OK)
class ShelvesController(
    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val shelfService: ShelfService
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(ShelvesController::class.java)

    @GetMapping("/getOne")
    fun getStock(
        @RequestParam(value = "row") rowNumber: Int,
        @RequestParam(value = "compartment") compartmentNumber: Int
    ): ProductDetails? {
        return shelfService.getStock(rowNumber, compartmentNumber)
    }

    @GetMapping("/get")
    fun getStocks(@RequestParam(value = "row") rowNumber: Int): List<ProductOnCompartmentDto> {
        return shelfService.getStocks(rowNumber)
    }

    @PostMapping("/autoMove_")
    fun autoMove(@RequestBody productID: ProductID): String {
        return shelfService.autoMove1(productID)
    }

    /**
     * Inputs:
     * - moveSlowSell: Whether to check for slow-selling.
     * - removeExpiring: Whether to remove expiring stocks.
     *
     * Requirements:
     * - Products must be in their correct categories if no other requirements.
     * - Compartment with no specified category can be filled with any products.
     * - Empty compartments left behind are to be filled by those products.
     * - Stocks of the same product are expected to be placed next to each other,
     *  preferably at the same compartment position across all layers.
     *
     * Implementation:
     * - Remove expiring stocks while store their previous locations
     * - Move slow-selling stocks to front compartments while store their previous locations
     * - Create Iterator iterating Compartments > Layers
     * - If moveSlowSell || removeExpiring {
     * -    compartmentsToFill = previous locations to items to be arranged
     * - } Else
     * -    compartmentsToFill = all empty compartments
     * - Prioritize Stock from newest package.
     *
     * Compartment Filling Implementation:
     * - Get newest package
     * - Get products from that
     * - For each product: record all compartments that can be filled by that product => Map<Product, Count>
     * - Results in Map<compartment, all products that can fill that compartment>
     *
     */
    @PostMapping("/autoMove")
    fun autoMoveMapping(
        @RequestParam(defaultValue = "False") slowSellCheck: Boolean,
        @RequestParam(defaultValue = "False") event: Boolean,
        @RequestParam(defaultValue = "True") autoMove: Boolean,
    ): String {
        return shelfService.autoMove(slowSellCheck, event, autoMove)
    }

    @PostMapping("/manualMove")
    fun move(@RequestBody body: JsonNode): String {
        return shelfService.move(body)
    }


    @PutMapping("/manualMove_")
    fun moveToShelf(@RequestBody body: MoveProductToShelfRequest): String {
        return shelfService.moveToShelf(body)
    }


    @PostMapping("/swap")
    fun swap(@RequestBody body: Iterable<SwapRequest>): String {
        return shelfService.swap(body)
    }

    @PutMapping("/moveToInventory")
    fun remove(@RequestBody body: Iterable<CompartmentPosition>): String {
        return shelfService.remove(body)
    }


    @PostMapping("/arrangeToFront")
    fun arrangeToFront(@RequestBody productList: ProductList): String {
        return shelfService.arrangeToFront(productList)
    }

    @PostMapping("/arrangeEventStocks")
    fun arrangeEventStocks(@RequestBody productList: ProductList): String {
        return shelfService.arrangeEventStocks(productList)
    }

    @PutMapping("/removeExpired")
    fun removeExpired() {
        shelfService.removeExpired()
    }

    @PutMapping("/removeAll")
    fun removeAll() {
        shelfService.removeAll()
    }

    @GetMapping("/print")
    fun printCompartments(@RequestParam(value = "row")
                          rowNumber: Int?): String {
        return shelfService.printCompartmentsOrAll(rowNumber)
    }

    @GetMapping("/printCategory")
    fun printCompartmentsCategory(@RequestParam(value = "row")
                                  rowNumber: Int?): String {
        return shelfService.printCompartmentsCategoryOrAll(rowNumber)
    }

}