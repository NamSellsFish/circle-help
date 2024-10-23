package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
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
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.api.response.ManualMoveRequest
import server.circlehelp.api.response.ProductDetails
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductList
import server.circlehelp.api.response.ProductOnCompartmentDto
import server.circlehelp.api.response.SwapRequest
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import server.circlehelp.utilities.ResponseBodyWriter
import server.circlehelp.utilities.Logic
import java.util.LinkedList
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/shelves")
class ShelvesController(
    private val productRepository: ProductRepository,
    @Autowired private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
    @Autowired private val shelvesRepository: ShelvesRepository,
    @Autowired private val rowRepository: RowRepository,
    @Autowired private val compartmentRepository: CompartmentRepository,
    @Autowired private val packageProductRepository: PackageProductRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,
    @Autowired private val mapperBuilder: Jackson2ObjectMapperBuilder,
    @Autowired private val responseBodyWriter: ResponseBodyWriter,
    @Autowired private val logic: Logic
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
            product.id!!,
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

                ProductOnCompartmentDto(
                    it.compartment.getLocation(),
                    ProductDetails(
                        order.product.id!!,
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
                        order.product.id!!,
                        order.product.name,
                        order.product.price,
                        order.wholesalePrice,
                        order.expirationDate
                    )
                } else null

                ProductOnCompartmentDto(
                    it.getLocation(),
                    productDetails
                )
            }

        return ResponseEntity.ok(objectMapper.writeValueAsString(collection))
    }

    @PostMapping("/automove")
    fun autoMove(@RequestBody productID: ProductID) : ResponseEntity<String> {
        val compartment = compartmentRepository
            .findAll()
            .firstOrNull { productOnCompartmentRepository.existsById(it.id!!) }
            ?: return ResponseEntity.ok("No empty compartments found.")

        val inventoryStock = inventoryRepository
            .findAllByOrderByPackageProductExpirationDateDesc()
            .firstOrNull { it.packageProduct.product.id == productID.id }
            ?: return responseBodyWriter.toResponseEntity(
                logic.productNotFoundResponse(productID.id))

        val result = moveToShelf(inventoryStock, compartment)

        if (result.first != null)
            return ResponseEntity.ok(result.first)
        return responseBodyWriter.toResponseEntity(result.second!!)
    }

    //TODO: Error aggregation
    @PutMapping("/manualMove")
    fun move(@RequestBody body: ManualMoveRequest) : ResponseEntity<String> {
        val inventoryStock =
            inventoryRepository
                .findAllByOrderByPackageProductExpirationDateDesc()
                .firstOrNull { it.packageProduct.product.id == body.src }
                ?: return ResponseEntity.badRequest().body("No product with id: ${body.src}")

        val compartmentPosition = body.des
        val compartmentResult = logic.getCompartment(compartmentPosition)
        val compartment = compartmentResult.first
            ?: return responseBodyWriter.toResponseEntity(compartmentResult.second!!)

        val result = moveToShelf(inventoryStock, compartment)
        if (result.first != null)
            return ResponseEntity.ok(result.first)
        return responseBodyWriter.toResponseEntity(result.second!!)
    }

    @PostMapping("/swap")
    fun swap(@RequestBody body: Iterable<SwapRequest>) : ResponseEntity<String> {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.add(changeStockPlacement(request.src, request.des))
        }

        if (errorResponse.errors.body.count() == 0) {
            return ResponseEntity.ok("")
        }

        return responseBodyWriter.toResponseEntity(errorResponse)
    }

    @PutMapping("/moveToInventory")
    fun remove(@RequestBody body: Iterable<CompartmentPosition>) : ResponseEntity<String> {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.add(moveToInventory(request).second)
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
                productOnCompartmentRepository.delete(compartment)
            }
        }
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

            moveToShelf(inventoryStock, compartment)

            if (!productIterator.hasNext()) {
                productIterator = products.iterator()
            }
        }
    }

    private fun moveToShelf(inventoryStock: InventoryStock, compartment: Compartment) : Pair<String?, ErrorResponse?> {

        if (inventoryStock.inventoryQuantity == 0) return logic.item("No items left to move.")

        if (logic.isExpiring(inventoryStock.packageProduct)) {
            return logic.error(
                logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
        }

        inventoryStock.inventoryQuantity--

        inventoryRepository.save(inventoryStock)

        val productOnCompartment = productOnCompartmentRepository.findByCompartment(compartment)

        if (productOnCompartment != null) {
            moveToInventory(productOnCompartment.compartment.getLocation())
        }

        productOnCompartmentRepository.save(ProductOnCompartment(
            compartment,
            inventoryStock.packageProduct
        ))

        return logic.item(objectMapper.writeValueAsString(compartment.getLocation()))
    }

    /*
    private fun moveToShelf(id: Long, location: CompartmentPosition) : ResponseEntity<String> {

        val compartment = compartmentRepository
            .findAll()
            .firstOrNull { i -> i.getLocation() == location }
            ?: return ResponseEntity.badRequest().body("No compartments found at location: $location")

        val inventoryStock = inventoryRepository
            .findAll()
            .firstOrNull { i -> i.packageProduct.product.id == id }
            ?: return ResponseEntity.badRequest().body("No product with id: $id")

        return moveToShelf(inventoryStock, compartment)
    }

     */

    private fun changeStockPlacement(oldLocation: CompartmentPosition, newLocation: CompartmentPosition) : ErrorResponse? {

        val oldCompartmentResult = logic.getCompartment(oldLocation)
        val oldCompartment = oldCompartmentResult.first
            ?: return oldCompartmentResult.second!!

        val oldProductOnCompartment =
            productOnCompartmentRepository.findByCompartment(oldCompartment)

        val newCompartmentResult = logic.getCompartment(newLocation)

        val newCompartment = newCompartmentResult.first
            ?: return newCompartmentResult.second!!

        val newProductOnCompartment =
            productOnCompartmentRepository.findByCompartment(newCompartment)

        var savedProductOnCompartmentStream = Stream.empty<ProductOnCompartment>()

        if (oldProductOnCompartment != null) {
            productOnCompartmentRepository.delete(oldProductOnCompartment)
            oldProductOnCompartment.compartment = newCompartment
            Stream.concat(savedProductOnCompartmentStream, Stream.of(oldProductOnCompartment))
                .also { savedProductOnCompartmentStream = it }
        }

        if (newProductOnCompartment != null) {
            productOnCompartmentRepository.delete(newProductOnCompartment)
            newProductOnCompartment.compartment = oldCompartment
            Stream.concat(savedProductOnCompartmentStream, Stream.of(newProductOnCompartment))
                .also { savedProductOnCompartmentStream = it }
        }

        for (productOnCompartment in savedProductOnCompartmentStream)
            productOnCompartmentRepository.save(productOnCompartment)

        return null
    }

    private fun moveToInventory(oldLocation: CompartmentPosition) : Pair<String?, ErrorResponse?> {
        val compartmentResult = logic.getCompartment(oldLocation)
        val compartment = compartmentResult.first
            ?: return logic.error(compartmentResult.second!!)

        val productOnCompartment = productOnCompartmentRepository
            .findByCompartment(compartment)
            ?: return logic.item("")

        val inventoryStock = inventoryRepository
            .findByPackageProduct(productOnCompartment.packageProduct)
            ?: return logic.item("")

        inventoryStock.inventoryQuantity++

        val repo = productOnCompartmentRepository
        repo.delete(
            repo.findByCompartment(compartment)!!
        )

        return logic.item("Removed product at compartment location: $oldLocation")
    }
}