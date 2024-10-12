package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ProductDetails
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductOnCompartmentDto
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import java.net.URI

@RestController
@RequestMapping("/api/shelves")
class ShelvesController(
    @Autowired private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
    @Autowired private val shelvesRepository: ShelvesRepository,
    @Autowired private val rowRepository: RowRepository,
    @Autowired private val compartmentRepository: CompartmentRepository,
    @Autowired private val packageProductRepository: PackageProductRepository,
    @Autowired private val mapperBuilder: Jackson2ObjectMapperBuilder
) {

    private val oldLocationTag = "oldLocation"
    private val newLocationTag = "newLocation"

    @GetMapping("/getOne")
    fun getStock(@RequestParam(value = "shelf") shelfNumber: Int,
                 @RequestParam(value = "row") rowNumber: Int,
                 @RequestParam(value = "compartment") compartmentNumber: Int) : ResponseEntity<String> {
        val shelf = shelvesRepository.findByNumber(shelfNumber)
            ?: return ResponseEntity.badRequest().body("No shelf with number $shelfNumber found.")
        val row = rowRepository.findByShelfAndNumber(shelf, rowNumber)
            ?: return ResponseEntity.badRequest().body("No shelf with number $shelfNumber found.")
        val compartment = compartmentRepository.findByLayerAndNumber(row, compartmentNumber)
            ?: return ResponseEntity.badRequest().body("No shelf with number $shelfNumber found.")

        val productOnCompartment = productOnCompartmentRepository.findByCompartment(compartment)
            ?: return ResponseEntity.badRequest().body("No shelf with number $shelfNumber found.")

        val packageProduct = packageProductRepository
            .findByOrderedPackageAndProduct(
                productOnCompartment.orderedPackage, productOnCompartment.product
            )!!

        val product = productOnCompartment.product
        val productDetails = ProductDetails(
            product.id!!,
            product.name,
            product.price,
            packageProduct.wholesalePrice,
            packageProduct.expirationDate
        )

        return ResponseEntity.ok(mapperBuilder.build<ObjectMapper>().writeValueAsString(productDetails))
    }


    @GetMapping("/get")
    fun getStocks(@RequestParam(value = "row") rowNumber: Int) : ResponseEntity<String> {

        val collection = productOnCompartmentRepository
            .findAll()
            .filter { i -> i.compartment.layer.number == rowNumber }
            .map(fun(i: ProductOnCompartment): ProductOnCompartmentDto {
                val packageProduct = packageProductRepository
                    .findByOrderedPackageAndProduct(
                        i.orderedPackage, i.product
                    )!!

                return ProductOnCompartmentDto(
                    i.compartment.getLocation(),
                    ProductDetails(
                        i.product.id!!,
                        i.product.name,
                        i.product.price,
                        packageProduct.wholesalePrice,
                        packageProduct.expirationDate
                    )
                )
            })

        return ResponseEntity.ok(mapperBuilder.build<ObjectMapper>().writeValueAsString(collection))
    }

    @PutMapping("/automove")
    fun autoMove(@RequestBody productID: ProductID) : ResponseEntity<String> {
        val compartment = compartmentRepository
            .findAll()
            .first {i -> productOnCompartmentRepository.existsById(i.id!!) }
            ?: return ResponseEntity.ok("No empty compartments found.")

        val inventoryStock = inventoryRepository
            .findAll()
            .first { i -> i.product.id == productID.id }
            ?: return ResponseEntity.badRequest().body("No product with id: ${productID.id}")

        return moveToShelf(inventoryStock, compartment)
    }

    @PutMapping("/move")
    fun move(@RequestBody body: Map<String, String>) : ResponseEntity<String> {
        if (!body.containsKey(oldLocationTag)) throw Error("$oldLocationTag not found.")

        val oldLocation = body[oldLocationTag]
        val newLocation = body[newLocationTag]


        return ResponseEntity(HttpStatus.NOT_IMPLEMENTED)
    }

    private fun moveToShelf(inventoryStock: InventoryStock, compartment: Compartment) : ResponseEntity<String> {

        if (inventoryStock.inventoryQuantity == 0) return ResponseEntity.ok("No items left to move.")

        inventoryStock.inventoryQuantity--

        inventoryRepository.save(inventoryStock)

        productOnCompartmentRepository.save(ProductOnCompartment(
            compartment,
            inventoryStock.product,
            inventoryStock.orderedPackage
        ))

        return ResponseEntity
            .created(URI.create("/api/shelves"))
            .body(mapperBuilder.build<ObjectMapper>().writeValueAsString(compartment.getLocation()))
    }

    private fun moveToShelf(id: Long, location: CompartmentPosition) : ResponseEntity<String> {

        val compartment = compartmentRepository
            .findAll()
            .first { i -> i.getLocation() == location }
            ?: return ResponseEntity.badRequest().body("No compartments found at location: ${location.toString()}")

        val inventoryStock = inventoryRepository
            .findAll()
            .first { i -> i.product.id == id }
            ?: return ResponseEntity.badRequest().body("No product with id: $id")

        return moveToShelf(inventoryStock, compartment)
    }

    private fun changeStockPlacement(oldLocation: CompartmentPosition, newLocation: CompartmentPosition) : ResponseEntity<String> {

        val productOnCompartment = productOnCompartmentRepository
            .findAll()
            .first { i -> i.compartment.getLocation() == oldLocation}
            ?: return ResponseEntity.badRequest().body("No product found at location: ${oldLocation.toString()}")

        val compartment = compartmentRepository
            .findAll()
            .first { i -> i.getLocation() == newLocation }
            ?: return ResponseEntity.badRequest().body("No compartments found at location: ${newLocation.toString()}")

        productOnCompartment.compartment = compartment

        productOnCompartmentRepository.save(productOnCompartment)

        return ResponseEntity.ok("Changed product location from ${oldLocation.toString()} to ${newLocation.toString()}")
    }

    private fun moveToInventory(oldLocation: CompartmentPosition) : ResponseEntity<String> {
        val compartment = compartmentRepository
            .findAll()
            .first { i -> i.getLocation() == oldLocation }
            ?: return ResponseEntity.badRequest().body("No compartments found at location: ${oldLocation.toString()}")

        val inventoryStock = inventoryRepository
            .findAll()
            .first { i -> i.product.id == mapperBuilder.build<ObjectMapper>().readValue(
                getStock(
                    oldLocation.shelfNumber,
                    oldLocation.rowNumber,
                    oldLocation.compartmentNumber
                ).body!!, ProductDetails::class.java).id }
            ?: return ResponseEntity.badRequest().body("No product at compartment location: ${oldLocation.toString()}")

        inventoryStock.inventoryQuantity++

        val repo = productOnCompartmentRepository
        repo.delete(
            repo.findByCompartment(compartment)!!
        )

        return ResponseEntity.ok("Removed product at compartment location: ${oldLocation.toString()}")
    }
}