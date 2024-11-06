package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
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
import java.util.stream.Stream

@Service
class ShelfService(
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
    private val logic: Logic,
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    fun moveToShelf(inventoryStock: InventoryStock, compartment: Compartment, displace: Boolean = true) : Pair<String?, ErrorResponse?> {

        if (inventoryStock.inventoryQuantity == 0) return logic.item("No items left to move.")

        if (logic.isExpiring(inventoryStock.packageProduct)) {
            return logic.error(
                logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
        }

        inventoryStock.inventoryQuantity--

        inventoryRepository.save(inventoryStock)

        val productOnCompartment = productOnCompartmentRepository.findByCompartment(compartment)

        if (productOnCompartment != null) {
            if (displace) {
                moveToInventory(productOnCompartment.compartment.getLocation())
            } else {
                return logic.item("Compartment is occupied.")
            }
        }

        productOnCompartmentRepository.save(
            ProductOnCompartment(
            compartment,
            inventoryStock.packageProduct
        )
        )

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

    fun changeStockPlacement(oldLocation: CompartmentPosition, newLocation: CompartmentPosition) : ErrorResponse? {

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

        /*
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
         */

        if (oldProductOnCompartment != null) {
            oldProductOnCompartment.compartment = newCompartment
            Stream.concat(savedProductOnCompartmentStream, Stream.of(oldProductOnCompartment))
                .also { savedProductOnCompartmentStream = it }
        }

        if (newProductOnCompartment != null) {
            newProductOnCompartment.compartment = oldCompartment
            Stream.concat(savedProductOnCompartmentStream, Stream.of(newProductOnCompartment))
                .also { savedProductOnCompartmentStream = it }
        }

        productOnCompartmentRepository.saveAll(Iterable{ savedProductOnCompartmentStream.iterator() })

        return null
    }

    fun moveToInventory(compartment: Compartment) : Pair<String?, ErrorResponse?> {

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

        return logic.item("Removed product at compartment location: ${compartment.getLocation()}")
    }

    fun moveToInventory(oldLocation: CompartmentPosition) : Pair<String?, ErrorResponse?> {
        val compartmentResult = logic.getCompartment(oldLocation)
        val compartment = compartmentResult.first
            ?: return logic.error(compartmentResult.second!!)

        return moveToInventory(compartment)
    }
}