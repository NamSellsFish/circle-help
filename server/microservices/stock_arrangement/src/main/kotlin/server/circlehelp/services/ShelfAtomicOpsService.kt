package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import jakarta.annotation.Resource
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import server.circlehelp.api.complement
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import java.util.stream.Stream

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
class ShelfAtomicOpsService(
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,

    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyPackageProductRepository: ReadonlyPackageProductRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val logic: Logic,
    @Qualifier("computationScheduler") private val scheduler: Scheduler,
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()

    private val logger = getLogger(ShelfAtomicOpsService::class.java)

    fun moveToShelf(inventoryStock: InventoryStock,
                    compartment: Compartment,
                    displace: Boolean = true,
                    allowExpiring: Boolean = false,
                    doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {

        if (logic.isExpiring(inventoryStock.packageProduct) && allowExpiring.complement()) {
            logger.info("Blocked attempt to move expired product to shelf: " + objectMapper.writeValueAsString(inventoryStock))
            return logic.error(
                logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
        }

        if (inventoryStock.inventoryQuantity == 1) {
            inventoryRepository.delete(inventoryStock)
        } else {
            inventoryStock.inventoryQuantity--
            inventoryRepository.save(inventoryStock)
        }

        val productOnCompartment = readonlyProductOnCompartmentRepository.findByCompartment(compartment)

        if (productOnCompartment != null) {
            if (displace) {
                moveToInventory(productOnCompartment.compartment.getLocation())
            } else {
                val message = "Tried to arrange '${inventoryStock.packageProduct.product.sku}' but compartment ${productOnCompartment.compartment.getLocation()} is occupied by '${productOnCompartment.packageProduct.product.sku}'"
                if (doLogging)
                    logger.info(message)
                return logic.item(message)
            }
        }

        productOnCompartmentRepository.save(
            ProductOnCompartment(
                compartment,
                inventoryStock.packageProduct
            )
        )

        val message = "Moved product '${inventoryStock.packageProduct.product.sku}' to compartment: ${compartment.getLocation()}"

        if (doLogging)
            logger.info(message)

        return logic.item(message)
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

    fun swapStockPlacement(oldLocation: CompartmentPosition, newLocation: CompartmentPosition, doLogging: Boolean = true) : ErrorResponse? {

        val oldCompartmentResult = logic.getCompartment(oldLocation)
        val oldCompartment = oldCompartmentResult.first
            ?: return oldCompartmentResult.second!!


        val newCompartmentResult = logic.getCompartment(newLocation)

        val newCompartment = newCompartmentResult.first
            ?: return newCompartmentResult.second!!

        return swapStockPlacement(oldCompartment, newCompartment)
    }

    fun swapStockPlacement(oldCompartment: Compartment, newCompartment: Compartment, doLogging: Boolean = true) : ErrorResponse? {
        val newProductOnCompartment =
            readonlyProductOnCompartmentRepository.findByCompartment(newCompartment)

        val oldProductOnCompartment =
            readonlyProductOnCompartmentRepository.findByCompartment(oldCompartment)

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

        val message = "Swapped ${oldCompartment.getLocation()} and ${newCompartment.getLocation()}"

        if (doLogging)
            logger.info(message)

        return null
    }


    fun moveToInventory(compartment: Compartment, doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {
        val productOnCompartment = readonlyProductOnCompartmentRepository
            .findByCompartment(compartment)
            ?: run {
                val message = "Attempted to remove from empty compartment ${compartment.getLocation()}"
                if (doLogging)
                    logger.info(message)
                return logic.item(message)
            }

        return moveToInventory(productOnCompartment, doLogging)
    }

    fun removeAll() : Observable<Unit> {

        return Observable.fromSingle(Observable.defer {
            Observable.fromIterable(readonlyProductOnCompartmentRepository.findAllId()).map { productOnCompartmentId ->
                val packageProductId =
                    readonlyProductOnCompartmentRepository.findPackageProductIdById(productOnCompartmentId)!!

                val inventoryStockId = readonlyInventoryRepository.findIdByPackageProductId(packageProductId)

                if (inventoryStockId != null) {
                    inventoryRepository.incrementQuantityById(inventoryStockId)
                } else {
                    inventoryRepository.save(InventoryStock(
                        readonlyPackageProductRepository.findById(packageProductId).get()))
                }

                Unit
            }
        }
            .subscribeOn(scheduler).count().map {
                logger.info("Delete Count: $it")
                productOnCompartmentRepository.deleteAll()
            })
    }

    fun moveToInventory(productOnCompartmentId: Long) : Pair<String?, ErrorResponse?> {

        val packageProductId = readonlyProductOnCompartmentRepository.findPackageProductIdById(productOnCompartmentId)
            ?: return logic.error(ErrorResponse("ID '$productOnCompartmentId' not found."))

        val inventoryStockId = readonlyInventoryRepository.findIdByPackageProductId(packageProductId)

        productOnCompartmentRepository.deleteById(productOnCompartmentId)

        if (inventoryStockId != null) {
            inventoryRepository.incrementQuantityById(inventoryStockId)
        } else {
            inventoryRepository.save(InventoryStock(
                readonlyPackageProductRepository.findById(packageProductId).get()))
        }

        return logic.item("")
    }

    fun moveToInventory(productOnCompartment: ProductOnCompartment, doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {

        val inventoryStock = readonlyInventoryRepository
            .findByPackageProduct(productOnCompartment.packageProduct)
            .let {
                it?.apply { inventoryQuantity++ }
                    ?: InventoryStock(productOnCompartment.packageProduct)
            }

        inventoryRepository.save(inventoryStock)

        productOnCompartmentRepository.delete(productOnCompartment)

        val message = "Removed product '${productOnCompartment.packageProduct.product.sku}' at compartment location: ${productOnCompartment.compartment.getLocation()}"

        if (doLogging)
            logger.info(message)

        return logic.item(message)
    }

    fun moveToInventory(oldLocation: CompartmentPosition, doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {
        val compartmentResult = logic.getCompartment(oldLocation)
        val compartment = compartmentResult.first
            ?: return logic.error(compartmentResult.second!!)

        return moveToInventory(compartment, doLogging)
    }
}