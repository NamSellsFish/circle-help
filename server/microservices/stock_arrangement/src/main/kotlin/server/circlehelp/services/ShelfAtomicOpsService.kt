package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.complement
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository.Companion.addTo
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import java.util.Optional
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

@Service
@RepeatableReadTransaction
class ShelfAtomicOpsService(
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,

    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyPackageProductRepository: ReadonlyPackageProductRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val logic: Logic,
    @Qualifier("computationScheduler") private val scheduler: Scheduler,
    private val applicationContext: ApplicationContext,
    private val callerService: CallerService
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()

    private val logger = getLogger(ShelfAtomicOpsService::class.java)

    val proxy : ShelfAtomicOpsService by lazy {
        val result = applicationContext.getBean(ShelfAtomicOpsService::class.java)
        if (result === this) throw AssertionError("Expected proxy, received original object.")
        result
    }

    val original : ShelfAtomicOpsService
        get() {
            return this
        }


    val compartments = HashSet<Compartment>()

    fun add(compartment: Compartment) {
        if (compartments.contains(compartment))
            throw DuplicateKeyException("${compartment.getLocation()}")
        compartments.add(compartment)
    }


    fun moveToShelf(inventoryStock: InventoryStock,
                    compartment: Compartment,
                    productOnCompartmentFunc: ProductOnCompartment.() -> Unit = {},
                    displace: Boolean = true,
                    allowExpiring: Boolean = false,
                    doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {

        return callerService.call {

            proxy.moveToInventory(compartment, doLogging = false)

            if (logic.isExpiring(inventoryStock.packageProduct) && allowExpiring.complement()) {
                logger.info("Blocked attempt to move expired product to shelf: " + objectMapper.writeValueAsString(inventoryStock))
                logic.error<String>(
                    logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
            }

            run {

                if (inventoryStock.inventoryQuantity == 1) {
                    inventoryRepository.delete(inventoryStock)
                } else {
                    inventoryStock.inventoryQuantity--
                    inventoryRepository.save(inventoryStock)
                }

                val productOnCompartment =
                    readonlyProductOnCompartmentRepository.findByCompartment(compartment)

                val newProductOnCompartment = productOnCompartment?.apply {
                    packageProduct = inventoryStock.packageProduct
                } ?:
                ProductOnCompartment(
                    compartment,
                    inventoryStock.packageProduct
                ).apply(productOnCompartmentFunc)

                productOnCompartmentRepository.save(newProductOnCompartment)
            }

            val message = "Moved product '${inventoryStock.packageProduct.product.sku}' of packageProduct '${inventoryStock.packageProduct.id}' to compartment: ${compartment.getLocation()}"

            if (doLogging)
                logger.info(message)

            logic.item(message)
        }
    }

    /**
     * Prerequisite: [compartments] must only contain unique elements.
     */
    fun moveToShelf(inventoryStock: InventoryStock,
                    compartments: Iterable<Compartment>,
                    productOnCompartmentFunc: ProductOnCompartment.() -> Unit = {},
                    displace: Boolean = true,
                    allowExpiring: Boolean = false,
                    doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {

        return callerService.call {

            if (logic.isExpiring(inventoryStock.packageProduct) && allowExpiring.complement()) {
                logger.info("Blocked attempt to move expired product to shelf: " + inventoryStock.packageProduct.id!!)
                logic.error<String>(
                    logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
            }

            //compartments.forEach { add(it) }

            //compartments.forEach { moveToInventory(it, false) }


            moveToInventory(StreamSupport.stream(compartments.spliterator(), false), true)
                .blockingSubscribe()

            val productOnCompartments = compartments.map {
                var counter = 1
                while (readonlyProductOnCompartmentRepository.existsByCompartment(it)) {

                    val delay = 100L
                    val time = counter * delay
                    Thread.sleep(delay)
                    logger.info("Waited for ${time}ms")
                    if (counter >= 50)
                        throw TimeoutException("Waited for ${time}ms")

                    counter++
                }

                ProductOnCompartment(
                    it,
                    inventoryStock.packageProduct
                ).apply(productOnCompartmentFunc)
            }

            val size = productOnCompartments.size

            if (inventoryStock.inventoryQuantity < size)
                logic.error<String>(logic.notEnoughInInventoryResponse(
                    inventoryStock.packageProduct.product.sku,
                    inventoryStock.packageProduct.orderedPackage.id!!
                ))

            if (inventoryStock.inventoryQuantity == size) {
                inventoryRepository.delete(inventoryStock)
            } else {
                inventoryStock.inventoryQuantity -= size
                inventoryRepository.save(inventoryStock)
            }

            productOnCompartmentRepository.saveAll(productOnCompartments)

            val message = productOnCompartments.joinToString("\n", prefix = "\n") {
                "Moved product '${inventoryStock.packageProduct.product.sku}' " +
                        "of packageProduct ${inventoryStock.packageProduct.id} " +
                        "to compartment: ${it.compartment.getLocation()}"
            }

            if (doLogging)
                logger.info(message)

            logic.item(message)
        }
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


    @RepeatableReadTransaction
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

        val map = listOf(newCompartment to newProductOnCompartment,
            oldCompartment to oldProductOnCompartment)

        val map2 = listOf(
                Triple(newCompartment,
                    newProductOnCompartment?.packageProduct,
                    newProductOnCompartment?.status),
                Triple(oldCompartment,
                    oldProductOnCompartment?.packageProduct,
                    oldProductOnCompartment?.status)
        )

        for (new in listOf(true, false)) {
            val (_, productOnCompartment) = map[if (new) 1 else 0]
            val (otherCompartment, packageProduct, status) = map2[if (!new) 1 else 0]
            if (productOnCompartment != null) {
                if (packageProduct == null || status == null) {
                    productOnCompartment.compartment = otherCompartment
                } else {

                    logger.info(productOnCompartment.packageProduct.product.sku)
                    logger.info(packageProduct.product.sku)

                    productOnCompartment.packageProduct = packageProduct
                    productOnCompartment.status = status
                }
                Stream.concat(savedProductOnCompartmentStream, Stream.of(productOnCompartment))
                    .also { savedProductOnCompartmentStream = it }
            }
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


    /**
     * Prerequisite: [compartments] must contain only unique elements.
     */
    @RepeatableReadTransaction
    fun moveToInventory(compartments: Stream<Compartment>, doLogging: Boolean = true) : Observable<Unit> {
        val productOnCompartments =
        Observable.fromStream(compartments)
            .mapOptional { Optional.ofNullable(readonlyProductOnCompartmentRepository
             .findByCompartment(it))
        }.toList()

        var addedStock = Observable.fromSingle(
            productOnCompartments.map {
                val x = it.stream().collect(
                    Collectors.groupingBy(
                        {it!!.packageProduct},
                        Collectors.counting()
                    )
                )

                x.entries.map { it.key.id to it.value }
                    .map { logger.info("ID ${it.first}, Count ${it.second}") }

                x.map {
                    (packageProduct, count) ->
                    val stock =
                        if (readonlyInventoryRepository.existsByPackageProduct(packageProduct))
                            readonlyInventoryRepository.findByPackageProduct(packageProduct)!!
                                .apply { inventoryQuantity += count.toInt() }
                        else
                            InventoryStock(packageProduct, count.toInt())

                    inventoryRepository.save(
                        stock
                    )
                Unit
            }
            productOnCompartmentRepository.deleteAll(it)
            logger.info("Moved to inventory:\n" + it.map{ it.compartment.getLocation() }.joinToString("\n", "\n"))
        })

        return addedStock
    }


    @RepeatableReadTransaction
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
            .count().map {
                logger.info("Delete Count: $it")
                productOnCompartmentRepository.deleteAll()
            })
    }


    @RepeatableReadTransaction
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


    @RepeatableReadTransaction
    fun moveToInventory(productOnCompartment: ProductOnCompartment, doLogging: Boolean = true) : Pair<String?, ErrorResponse?> {

        run {
            val inventoryStock = readonlyInventoryRepository
                .findByPackageProduct(productOnCompartment.packageProduct)
                .let {
                    it?.apply { inventoryQuantity++ }
                        ?: InventoryStock(productOnCompartment.packageProduct)
                }

            inventoryRepository.save(inventoryStock)

            productOnCompartmentRepository.delete(productOnCompartment)
        }

        val message = "Removed product '${productOnCompartment.packageProduct.product.sku}' " +
                "of packageProduct '${productOnCompartment.packageProduct.id}' " +
                "at compartment location: ${productOnCompartment.compartment.getLocation()}"

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