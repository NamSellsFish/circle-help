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
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.api.complement
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorsResponse
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.caches.CompartmentProductCategoryCache
import server.circlehelp.repositories.caches.EventCompartmentCache
import server.circlehelp.repositories.caches.PackageProductCache
import server.circlehelp.repositories.caches.ProductCategorizationCache
import server.circlehelp.repositories.readonly.ReadonlyCompartmentProductCategoryRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import java.util.Optional
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

@Service
@ManagementRequiredTransaction
class ShelfAtomicOpsService(
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,

    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyPackageProductRepository: PackageProductCache,
    private val eventCompartmentCache: EventCompartmentCache,
    private val readonlyCompartmentProductCategoryRepository: CompartmentProductCategoryCache,
    private val readonlyProductCategorizationRepository: ProductCategorizationCache,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val logic: Logic,
    @Qualifier("computationScheduler") private val scheduler: Scheduler,
    private val applicationContext: ApplicationContext,
    private val transactionService: TransactionService,

    private val entityManager: EntityManager,
    private val activeEventsService: ActiveEventsService,
    val statusArbiterManager: StatusArbiterManager,
) {
    private val objectMapper = mapperBuilder.build<ObjectMapper>()

    private val logger = getLogger(ShelfAtomicOpsService::class.java)

    private val statusDecider = statusArbiterManager.topStatusArbiter

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
                    doLogging: Boolean = true) : Pair<String?, ErrorsResponse?> {

        return transactionService.managementRequired {

            proxy.moveToInventory(compartment, doLogging = false)

            if (logic.isExpiring(inventoryStock.packageProduct) && allowExpiring.complement()) {
                logger.info("Blocked attempt to move expired product to shelf: " +
                        "SKU ${inventoryStock.packageProduct.product.sku} of Package ${inventoryStock.packageProduct.orderedPackage.id}")
                logic.error<String>(
                    logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
            }

            val message = "Moving product '${inventoryStock.packageProduct.product.sku}' of packageProduct '${inventoryStock.packageProduct.orderedPackage.id}' to compartment: ${compartment.getLocation()}"

            run {

                if (inventoryStock.inventoryQuantity == 1) {
                    inventoryRepository.delete(inventoryStock)
                } else {

                    inventoryRepository.save(
                        inventoryStock.apply { inventoryQuantity -= 1 }
                    )
                }

                val productOnCompartment =
                    readonlyProductOnCompartmentRepository.findByCompartment(compartment)

                val newProductOnCompartment =
                ProductOnCompartment(
                    compartment,
                    inventoryStock.packageProduct,
                    statusDecider.decide(inventoryStock.packageProduct, compartment, 1),
                    productOnCompartment?.version ?: 0
                )

                productOnCompartmentRepository.save(newProductOnCompartment)
            }

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
                    doLogging: Boolean = true) : Pair<String?, ErrorsResponse?> {

        return transactionService.managementRequired {

            if (logic.isExpiring(inventoryStock.packageProduct) && allowExpiring.complement()) {
                logger.info("Blocked attempt to move expired product to shelf: " +
                        "SKU ${inventoryStock.packageProduct.product.sku} of Package ${inventoryStock.packageProduct.orderedPackage.id}")
                logic.error<String>(
                    logic.expiredProductArrangementAttemptResponse(inventoryStock.packageProduct))
            }

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
                    inventoryStock.packageProduct,
                    statusDecider.decide(inventoryStock.packageProduct, it, 1)
                ).apply(productOnCompartmentFunc)
            }


            val message = productOnCompartments.joinToString("\n", prefix = "\n") {
                "Moving product '${inventoryStock.packageProduct.product.sku}' " +
                        "of packageProduct ${inventoryStock.packageProduct.id} " +
                        "to compartment: ${it.compartment.getLocation()}"
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

    fun swapStockPlacement(oldLocation: CompartmentPosition, newLocation: CompartmentPosition, doLogging: Boolean = true) : ErrorsResponse? {

        val oldCompartmentResult = logic.getCompartment(oldLocation)
        val oldCompartment = oldCompartmentResult.first
            ?: return oldCompartmentResult.second!!


        val newCompartmentResult = logic.getCompartment(newLocation)

        val newCompartment = newCompartmentResult.first
            ?: return newCompartmentResult.second!!

        return swapStockPlacement(oldCompartment, newCompartment)
    }


    @ManagementRequiredTransaction
    fun swapStockPlacement(oldCompartment: Compartment, newCompartment: Compartment,
                           srcStatus: Int? = null,
                           desStatus: Int? = null,
                           doLogging: Boolean = true) : ErrorsResponse? {

        val newProductOnCompartment =
            readonlyProductOnCompartmentRepository.findByCompartment(newCompartment)

        val oldProductOnCompartment =
            readonlyProductOnCompartmentRepository.findByCompartment(oldCompartment)

        logger.info("${newProductOnCompartment?.compartment?.id}")
        logger.info("${oldProductOnCompartment?.compartment?.id}")

        var savedProductOnCompartmentStream = Stream.empty<ProductOnCompartment>()

        val message = "Swapping ${oldCompartment.getLocation()}, " +
                (if (oldProductOnCompartment != null)
                    "status ${oldProductOnCompartment.status} ${desStatus?.let { "to $desStatus" } ?: ""}, sku ${oldProductOnCompartment.packageProduct.product.sku}"
                else "") +
                "and ${newCompartment.getLocation()}, " +
                (if (newProductOnCompartment != null)
                    "status ${newProductOnCompartment.status} ${srcStatus?.let { "to $srcStatus" } ?: ""}, sku ${newProductOnCompartment.packageProduct.product.sku}"
                else "")

        if (doLogging)
            logger.info(message)

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

        val altStatus = listOf(desStatus, srcStatus)

        for (new in listOf(true, false)) {
            var (compartment, productOnCompartment) = map[if (new) 0 else 1]
            var (otherCompartment, packageProduct, status) = map2[if (!new) 0 else 1]
            if (productOnCompartment != null) {
                if (packageProduct == null || status == null) {
                    logger.info("Swapped to empty.")
                    productOnCompartmentRepository.delete(productOnCompartment)
                    productOnCompartment = ProductOnCompartment(
                        otherCompartment,
                        productOnCompartment.packageProduct,
                        statusDecider.decide(
                            productOnCompartment.packageProduct,
                            otherCompartment,
                            altStatus[if (!new) 0 else 1] ?: productOnCompartment.status
                        )
                    )
                } else {

                    logger.info("This SKU: " + productOnCompartment.packageProduct.product.sku)
                    logger.info("Other SKU: " + packageProduct.product.sku)

                    productOnCompartment = productOnCompartment.with(
                        packageProduct = packageProduct,
                        status = statusDecider.decide(
                        packageProduct, compartment,
                        altStatus[if (new) 0 else 1] ?: status)
                    )
                }
                Stream.concat(savedProductOnCompartmentStream, Stream.of(productOnCompartment))
                    .also { savedProductOnCompartmentStream = it }
            }
        }

        productOnCompartmentRepository.saveAll(Iterable{ savedProductOnCompartmentStream.iterator() })

        return null
    }

    fun checkedSwap(srcCompartment: Compartment, desCompartment: Compartment) {

        val desProductOnCompartment = readonlyProductOnCompartmentRepository.findByCompartment(desCompartment)

        if (desProductOnCompartment != null) {

            val desProductCategories =
                readonlyProductCategorizationRepository.findAllByProductAsProductCategorySet(
                    desProductOnCompartment.packageProduct.product
                )

            val srcCategory =
                readonlyCompartmentProductCategoryRepository.findByCompartment(srcCompartment)
                    ?.productCategory

            if (srcCategory != null && desProductCategories.contains(srcCategory).not())
                moveToInventory(desCompartment)
        }

        swapStockPlacement(srcCompartment, desCompartment, null, 1)
    }


    fun moveToInventory(compartment: Compartment, doLogging: Boolean = true) : Pair<String?, ErrorsResponse?> {
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
    @ManagementRequiredTransaction
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

                x.entries.map { Triple(it.key.orderedPackage.id, it.key.product.sku, it.value) }
                    .map { logger.info("Package ${it.first}, SKU ${it.second} , Count ${it.third}") }

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


    @ManagementRequiredTransaction
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


    @ManagementRequiredTransaction
    fun moveToInventory(productOnCompartmentId: Long) : Pair<String?, ErrorsResponse?> {

        val packageProductId = readonlyProductOnCompartmentRepository.findPackageProductIdById(productOnCompartmentId)
            ?: return logic.error(ErrorsResponse("ID '$productOnCompartmentId' not found."))

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


    @ManagementRequiredTransaction
    fun moveToInventory(productOnCompartment: ProductOnCompartment, doLogging: Boolean = true) : Pair<String?, ErrorsResponse?> {

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
                "of packageProduct '${productOnCompartment.packageProduct.orderedPackage.id}' " +
                "at compartment location: ${productOnCompartment.compartment.getLocation()}"

        if (doLogging)
            logger.info(message)

        return logic.item(message)
    }

    fun moveToInventory(oldLocation: CompartmentPosition, doLogging: Boolean = true) : Pair<String?, ErrorsResponse?> {
        val compartmentResult = logic.getCompartment(oldLocation)
        val compartment = compartmentResult.first
            ?: return logic.error(compartmentResult.second!!)

        return moveToInventory(compartment, doLogging)
    }
}