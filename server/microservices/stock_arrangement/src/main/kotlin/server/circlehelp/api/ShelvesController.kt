package server.circlehelp.api

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.api.response.CompartmentInfo
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.api.response.ManualMovesRequest
import server.circlehelp.api.response.MoveProductToShelfRequest
import server.circlehelp.api.response.ProductDetails
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductList
import server.circlehelp.api.response.ProductOnCompartmentDto
import server.circlehelp.api.response.SwapRequest
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.Product
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.Logic
import server.circlehelp.services.ShelfService
import java.util.LinkedList
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/shelves")
class ShelvesController(
    private val productRepository: ProductRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,
    private val shelvesRepository: ShelvesRepository,
    private val rowRepository: RowRepository,
    private val compartmentRepository: CompartmentRepository,
    private val packageProductRepository: PackageProductRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val responseBodyWriter: ResponseBodyWriter,
    private val logic: Logic,
    private val shelfService: ShelfService,
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()

    @GetMapping("/getOne")
    fun getStock(@RequestParam(value = "shelf") shelfNumber: Int,
                 @RequestParam(value = "row") rowNumber: Int,
                 @RequestParam(value = "compartment") compartmentNumber: Int) : ResponseEntity<String> {

        val compartmentResult =
            logic.getCompartment(
            CompartmentPosition(shelfNumber, rowNumber, compartmentNumber))

        val compartment = compartmentResult.first
            ?: return responseBodyWriter.toResponseEntity(compartmentResult.second!!)

        val productOnCompartment = productOnCompartmentRepository.findByCompartment(compartment)
            ?: return ResponseEntity.ok("")

        val packageProduct = productOnCompartment.packageProduct

        val product = packageProduct.product
        val productDetails = ProductDetails(
            product.sku,
            product.name,
            product.price,
            packageProduct.wholesalePrice,
            packageProduct.expirationDate
        )

        return ResponseEntity.ok(objectMapper.writeValueAsString(productDetails))
    }

    @GetMapping("/get")
    fun getStocks(@RequestParam(value = "row") rowNumber: Int) : ResponseEntity<String> {

        return getStocks2(rowNumber)
    }

    private fun getStocks1(rowNumber: Int) : ResponseEntity<String> {
        val collection = productOnCompartmentRepository
            .findAll()
            .filter { it.compartment.layer.number == rowNumber }
            .map {
                logic.updateExpiration(it)

                val order = it.packageProduct

                val location = it.compartment.getLocation()

                ProductOnCompartmentDto(
                    CompartmentInfo(
                        location.shelfNo,
                        location.rowNo,
                        location.compartmentNo,
                        it.compartment.compartmentNoFromUserPerspective
                    ),
                    ProductDetails(
                        order.product.sku,
                        order.product.name,
                        order.product.price,
                        order.wholesalePrice,
                        order.expirationDate
                    )
                )
            }

        return ResponseEntity.ok(objectMapper.writeValueAsString(collection))
    }

    private fun getStocks2(rowNumber: Int) : ResponseEntity<String> {
        val collection = compartmentRepository
            .findAll()
            .filter { it.layer.number == rowNumber }
            .map {

                val productOnCompartment = productOnCompartmentRepository.findByCompartment(it)

                if (productOnCompartment != null)
                    logic.updateExpiration(productOnCompartment)

                val order = productOnCompartment?.packageProduct

                val productDetails : ProductDetails? = if (order != null) {
                    ProductDetails(
                        order.product.sku,
                        order.product.name,
                        order.product.price,
                        order.wholesalePrice,
                        order.expirationDate
                    )
                } else null

                val location = it.getLocation()

                ProductOnCompartmentDto(
                    CompartmentInfo(
                        location.shelfNo,
                        location.rowNo,
                        location.compartmentNo,
                        it.compartmentNoFromUserPerspective
                    ),
                    productDetails
                )
            }

        return ResponseEntity.ok(objectMapper.writeValueAsString(collection))
    }

    @PostMapping("/automove_")
    fun autoMove(@RequestBody productID: ProductID) : ResponseEntity<String> {
        val compartment = compartmentRepository
            .findAll()
            .firstOrNull { productOnCompartmentRepository.findByCompartment(it) == null }
            ?: return ResponseEntity.ok("No empty compartments found.")

        val inventoryStock = inventoryRepository
            .findAllByOrderByPackageProductExpirationDateDesc()
            .firstOrNull { it.packageProduct.product.sku == productID.sku }
            ?: return responseBodyWriter.toResponseEntity(
                logic.notInInventoryResponse(productID.sku))

        val result = shelfService.moveToShelf(inventoryStock, compartment)

        if (result.first != null)
            return ResponseEntity.ok(result.first)
        return responseBodyWriter.toResponseEntity(result.second!!)
    }

    /**
     * Inputs:
     * - List of IDs of products to be arranged to the shelves from the inventory.
     * - List of IDs of products to be checked for slow-selling.
     *  If empty, all products on shelves will be checked.
     * - moveSlowSell: Whether to check for slow-selling.
     * - removeExpiring: Whether to remove expiring stocks
     *
     * Requirements:
     * - Products must be in their correct categories if no other requirements.
     * - Empty compartments left behind are to be filled by those products.
     * - Stocks of the same product are expected to be placed next to each other,
     *  preferably at the same compartment position across all layers.
     *
     * Implementation:
     * - Remove expiring stocks while store their previous locations
     * - Move slow-selling stocks to front compartments while store their previous locations
     * - Create Iterator iterating Shelves > Compartments > Layers
     * - If moveSlowSell || removeExpiring {
     * - Partition previous locations to items to be arranged
     * - } Else
     * - Move Items to correct empty compartments of that category
     */
    @PostMapping("/automove")
    fun autoMove() {
        objectMapper.readTree()
    }

    @PostMapping("/manualMove")
    fun move(@RequestBody body: ManualMovesRequest) : ResponseEntity<String> {

        val srcIterator = body.src.iterator()
        val desIterator = body.des.iterator()

        val errors = ErrorResponse()

        while (srcIterator.hasNext() && desIterator.hasNext()) {

            val src = srcIterator.next()
            val des = desIterator.next()

            if (src.length == 6 && des != null) {

                val desPos: CompartmentPosition

                try {
                    desPos = objectMapper.readValue<CompartmentPosition>(des)
                } catch (ex: DatabindException) {
                    errors.add(ErrorResponse(ex.localizedMessage))
                    continue
                }

                val result = moveToShelf(src, desPos)

                if (result.first == null) {
                    errors.add(result.second!!)
                }
            }
            else
            if (des == null) {

                val srcPos: CompartmentPosition

                try {
                    srcPos = objectMapper.readValue<CompartmentPosition>(src)
                } catch (ex: DatabindException) {
                    errors.add(ErrorResponse(ex.localizedMessage))
                    continue
                }

                val result = shelfService.moveToInventory(srcPos)

                if (result.first == null) {
                    errors.add(result.second!!)
                }
            }
        }

        if (srcIterator.hasNext()) {
            errors.add(ErrorResponse("Source List has more items than Destination List"))
        }

        if (desIterator.hasNext()) {
            errors.add(ErrorResponse("Destination List has more items than Source List"))
        }

        if (! errors.errors.body.any()) {
            return ResponseEntity.ok("")
        } else {
            return responseBodyWriter.toResponseEntity(errors)
        }
    }


    @PutMapping("/manualMove_")
    fun moveToShelf(@RequestBody body: MoveProductToShelfRequest) : ResponseEntity<String> {

        val result = moveToShelf(body.src, body.des)

        val message = result.first
            ?: return responseBodyWriter.toResponseEntity(result.second!!)

        return ResponseEntity.ok(message)
    }

    private fun moveToShelf(sku: String, compartmentPosition: CompartmentPosition) : Pair<String?, ErrorResponse?> {
        val inventoryStock =
            inventoryRepository
                .findAllByOrderByPackageProductExpirationDateDesc()
                .firstOrNull { it.packageProduct.product.sku == sku }
                ?: return logic.error(logic.productNotFoundResponse(sku))

        val compartmentResult = logic.getCompartment(compartmentPosition)
        val compartment = compartmentResult.first
            ?: return logic.error(compartmentResult.second!!)

        val result = shelfService.moveToShelf(inventoryStock, compartment)
        if (result.first != null)
            return logic.item(result.first)
        return logic.error(result.second!!)
    }


    @PostMapping("/swap")
    fun swap(@RequestBody body: Iterable<SwapRequest>) : ResponseEntity<String> {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.add(shelfService.changeStockPlacement(request.src, request.des))
        }

        if (! errorResponse.errors.body.any()) {
            return ResponseEntity.ok("")
        }

        return responseBodyWriter.toResponseEntity(errorResponse)
    }

    @PutMapping("/moveToInventory")
    fun remove(@RequestBody body: Iterable<CompartmentPosition>) : ResponseEntity<String> {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.add(shelfService.moveToInventory(request).second)
        }

        if (errorResponse.errors.body.count() == 0) {
            return ResponseEntity.ok("")
        }

        return responseBodyWriter.toResponseEntity(errorResponse)
    }


    @PostMapping("/arrangeToFront")
    fun arrangeToFront(@RequestBody productList: ProductList) : ResponseEntity<String> {

        return validateAndArrangeProductList(productList, frontCompartmentRepository.findAll().map { it.compartment })
    }

    @PostMapping("/arrangeEventStocks")
    fun arrangeEventStocks(@RequestBody productList: ProductList) : ResponseEntity<String> {

        return validateAndArrangeProductList(productList, eventCompartmentRepository.findAll().map { it.compartment })
    }

    @PutMapping("/removeExpired")
    fun removeExpired() {
        for (compartment in productOnCompartmentRepository.findAll()) {
            if (logic.isExpiring(compartment.packageProduct)) {
                shelfService.moveToInventory(compartment.compartment)
            }
        }
    }

    @PutMapping("/removeAll")
    fun removeAll() {
        for (compartment in productOnCompartmentRepository.findAll()) {
            shelfService.moveToInventory(compartment.compartment)
        }
    }

    @GetMapping("/print")
    fun printCompartments(@RequestParam(value = "row") rowNumber: Int) : ResponseEntity<String> {

        val collection = MutableList<LinkedList<String?>>(shelvesRepository.count().toInt()) { LinkedList() }

        val compartments = compartmentRepository
            .findAll()
            .filter { it.layer.number == rowNumber }

        for (compartment in compartments) {
            val productOnCompartment = productOnCompartmentRepository.findByCompartment(compartment)
            collection[compartment.layer.shelf.number - 1].addLast(productOnCompartment?.packageProduct?.product?.sku)
        }

        val result = collection.map {
            it.joinToString()
        }

        return ResponseEntity.ok(objectMapper.writeValueAsString(result))
    }

    private fun validateAndArrangeProductList(productList: ProductList, compartments: Iterable<Compartment>) : ResponseEntity<String> {
        val list: LinkedList<Product> = LinkedList()

        for (productID in productList.productList) {
            val product = productRepository.findById(productID).getOrNull()
                ?: return responseBodyWriter.toResponseEntity(logic.productNotFoundResponse(productID))
            list.add(product)
        }

        continuousArrangement(list, compartments)

        return ResponseEntity.ok("")
    }

    private fun continuousArrangement(products: LinkedList<Product>, compartments: Iterable<Compartment>) {

        //val productsToRearrange = HashMap<Product, int>()




        var productIterator = products.iterator()
        val compartmentIterator = compartments.iterator()

        while (productIterator.hasNext() && compartmentIterator.hasNext()) {

            val product = productIterator.next()

            val inventoryStock =
                inventoryRepository
                    .findAllByOrderByPackageProductExpirationDateDesc()
                    .firstOrNull { it.packageProduct.product == product
                            && it.inventoryQuantity > 0 }

            if (inventoryStock == null) {
                productIterator.remove()
                continue
            }

            val compartment = compartmentIterator.next()

            shelfService.moveToShelf(inventoryStock, compartment)

            if (!productIterator.hasNext()) {
                productIterator = products.iterator()
            }
        }
    }

    private fun continuousArrangement1(products: LinkedList<Product>, compartments: Iterable<Compartment>) {
        var productIterator = products.iterator()
        val compartmentIterator = compartments.iterator()

        while (productIterator.hasNext() && compartmentIterator.hasNext()) {

            val product = productIterator.next()

            val inventoryStock =
                inventoryRepository
                .findAllByOrderByPackageProductExpirationDateDesc()
                .firstOrNull { it.packageProduct.product == product
                        && it.inventoryQuantity > 0 }

            if (inventoryStock == null) {
                productIterator.remove()
                continue
            }

            val compartment = compartmentIterator.next()

            shelfService.moveToShelf(inventoryStock, compartment)

            if (!productIterator.hasNext()) {
                productIterator = products.iterator()
            }
        }
    }

}