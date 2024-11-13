package server.circlehelp.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
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

@RestController
@RequestMapping("$baseURL$shelf")
class ShelvesController(
    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val shelfService: ShelfService,
    private val responseBodyWriter: ResponseBodyWriter
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(ShelvesController::class.java)


    @GetMapping("/getOne")
    fun getStock(
        @RequestParam(value = "row") rowNumber: Int,
        @RequestParam(value = "compartment") compartmentNumber: Int
    ): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.getStock(rowNumber, compartmentNumber)
        }
    }

    @GetMapping("/get")
    fun getStocks(@RequestParam(value = "row") rowNumber: Int): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.getStocks(rowNumber)
        }
    }

    @PostMapping("/autoMove_")
    fun autoMove(@RequestBody productID: ProductID): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.autoMove1(productID)
        }
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
        @RequestParam(defaultValue = "False") event: Boolean
    ): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.autoMove(slowSellCheck, event)
        }
    }

    @PostMapping("/manualMove")
    fun move(@RequestBody body: JsonNode): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.move(body)
        }
    }


    @PutMapping("/manualMove_")
    fun moveToShelf(@RequestBody body: MoveProductToShelfRequest): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.moveToShelf(body)
        }
    }


    @PostMapping("/swap")
    fun swap(@RequestBody body: Iterable<SwapRequest>): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.swap(body)
        }
    }

    @PutMapping("/moveToInventory")
    fun remove(@RequestBody body: Iterable<CompartmentPosition>): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.remove(body)
        }
    }


    @PostMapping("/arrangeToFront")
    fun arrangeToFront(@RequestBody productList: ProductList): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            productList
        }
    }

    @PostMapping("/arrangeEventStocks")
    fun arrangeEventStocks(@RequestBody productList: ProductList): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.arrangeEventStocks(productList)
        }
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
    fun printCompartments(@RequestParam(value = "row") rowNumber: Int): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.printCompartments(rowNumber)
        }
    }

    @GetMapping("/printCategory")
    fun printCompartmentsCategory(@RequestParam(value = "row") rowNumber: Int): ResponseEntity<String> {
        return responseBodyWriter.wrap {
            shelfService.printCompartmentsCategory(rowNumber)
        }
    }

}