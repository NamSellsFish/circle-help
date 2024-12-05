package server.circlehelp.services

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.complement
import server.circlehelp.api.request.PackageProductItem
import server.circlehelp.api.response.CompartmentInfo
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.api.response.ErrorsResponse
import server.circlehelp.api.response.ErrorsResponseException
import server.circlehelp.api.response.MoveProductToShelfRequest
import server.circlehelp.api.response.ProductDetails
import server.circlehelp.api.response.ProductID
import server.circlehelp.api.response.ProductList
import server.circlehelp.api.response.ProductOnCompartmentDto
import server.circlehelp.api.response.SwapRequest
import server.circlehelp.configuration.BeanQualifiers
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.caches.ArrivedPackageCache
import server.circlehelp.repositories.caches.CompartmentCache
import server.circlehelp.repositories.caches.CompartmentProductCategoryCache
import server.circlehelp.repositories.caches.EventCompartmentCache
import server.circlehelp.repositories.caches.FrontCompartmentCache
import server.circlehelp.repositories.caches.InventoryStockCache
import server.circlehelp.repositories.caches.LayerCache
import server.circlehelp.repositories.caches.PackageProductCache
import server.circlehelp.repositories.caches.ProductCache
import server.circlehelp.repositories.caches.ProductCategorizationCache
import server.circlehelp.repositories.caches.ProductOnCompartmentCache
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import java.util.HashMap
import java.util.LinkedList
import kotlin.jvm.Throws
import kotlin.jvm.optionals.getOrNull

@Service
@RepeatableReadTransaction
class ShelfService(
    private val productCache: ProductCache,
    private val productOnCompartmentCache: ProductOnCompartmentCache,
    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val inventoryStockCache: InventoryStockCache,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyRowRepository: LayerCache,
    private val compartmentCache: CompartmentCache,
    private val packageProductCache: PackageProductCache,
    private val frontCompartmentCache: FrontCompartmentCache,
    private val eventCompartmentCache: EventCompartmentCache,
    private val compartmentProductCategoryCache: CompartmentProductCategoryCache,
    private val readonlyProductCategorizationRepository: ProductCategorizationCache,
    private val arrivedPackageCache: ArrivedPackageCache,

    private val productOnCompartmentRepository: ProductOnCompartmentRepository,


    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val logic: Logic,
    private val shelfAtomicOpsService: ShelfAtomicOpsService,
    private val blocs: Blocs,

    @Qualifier(BeanQualifiers.computationScheduler) private val computationScheduler: Scheduler,
    @Qualifier(BeanQualifiers.sameThreadScheduler) private val sameThreadScheduler: Scheduler,
    private val entityManager: EntityManager,
    private val activeEventsService: ActiveEventsService,
    private val tableAuditingService: TableAuditingService,
    statusArbiterManager: StatusArbiterManager,
    private val transactionService: TransactionService
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()
    private val logger = LoggerFactory.getLogger(ShelfService::class.java)

    private val statusDecider = statusArbiterManager.topStatusArbiter

    @RepeatableReadTransaction(readOnly = true)
    fun clearCache() {
        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )

        inventoryStockCache.update()
        productOnCompartmentCache.update()
    }

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun getStock(
        rowNumber: Int,
        compartmentNumber: Int
    ): ProductDetails? {

        productCache.checkTables()

        val compartmentResult =
            logic.getCompartment(
                CompartmentPosition(rowNumber, compartmentNumber)
            )

        val compartment = compartmentResult.first
            ?: throw ErrorsResponseException(compartmentResult.second!!)

        val productOnCompartment =
            productOnCompartmentCache.findByCompartment(compartment)
                ?: return null

        val packageProduct = productOnCompartment.packageProduct

        val product = packageProduct.product

        return ProductDetails(
            packageProduct.orderedPackage.id!!,
            product.sku,
            product.name,
            product.price,
            packageProduct.wholesalePrice,
            packageProduct.expirationDate
        )
    }

    @RepeatableReadTransaction(readOnly = true)
    fun getStocks(rowNumber: Int) : List<ProductOnCompartmentDto> {

        return getStocks2(rowNumber)
    }

    private fun getStocks1(rowNumber: Int): List<ProductOnCompartmentDto> {

        compartmentProductCategoryCache.checkTables()

        return productOnCompartmentCache.apply { checkTables() }
            .findAll()
            .filter { it.compartment.layer.number == rowNumber }
            .map {
                logic.updateExpiration(it)

                val order = it.packageProduct

                val location = it.compartment.getLocation()

                ProductOnCompartmentDto(
                    CompartmentInfo(
                        blocs.getShelfCharIndex(it.compartment.number),
                        location.rowNo,
                        location.compartmentNo,
                        it.compartment.compartmentNoFromUserPerspective
                    ),
                    it.status,
                    compartmentProductCategoryCache
                        .findByCompartment(it.compartment)
                        ?.productCategory?.name ?: "",
                    ProductDetails(
                        order.orderedPackage.id!!,
                        order.product.sku,
                        order.product.name,
                        order.product.price,
                        order.wholesalePrice,
                        order.expirationDate
                    )
                )
            }
    }

    private fun getStocks2(rowNumber: Int) : List<ProductOnCompartmentDto> {

        productOnCompartmentCache.checkTables()
        compartmentProductCategoryCache.checkTables()

        return compartmentCache.apply { checkTables() }
            .findAll()
            .filter { it.layer.number == rowNumber }
            .map {

                val productOnCompartment = productOnCompartmentCache
                    .findByCompartment(it)

                /*
            val activeEventProducts = activeEventsService.activeProductsSet

            if (productOnCompartment != null) {
                logic.updateEventStatus(productOnCompartment, activeEventProducts)
                logic.updateExpiration(productOnCompartment)
            }
             */

                val order = productOnCompartment?.packageProduct

                val productDetails: ProductDetails? = if (order != null) {
                    ProductDetails(
                        order.orderedPackage.id!!,
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
                        blocs.getShelfCharIndex(it.number),
                        location.rowNo,
                        location.compartmentNo,
                        it.compartmentNoFromUserPerspective
                    ),
                    productOnCompartment?.status ?: 0,
                    compartmentProductCategoryCache
                        .findByCompartment(it)
                        ?.productCategory?.name ?: "",
                    productDetails
                )

            }
    }

    @Throws(ErrorsResponseException::class)
    fun autoMove1(productID: ProductID) : String {

        val compartment = compartmentCache.apply { checkTables() }
            .findAll()
            .firstOrNull { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement()  }
            ?: return "No empty compartments found."

        val inventoryStock = inventoryStockCache.apply { checkTables() }
            .findAllByOrderByPackageProductExpirationDateDesc()
            .firstOrNull { it.packageProduct.product.sku == productID.sku }
            ?: throw ErrorsResponseException(
                logic.notInInventoryResponse(productID.sku))

        val result = shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)

        tableAuditingService.updateTableAudit<ProductOnCompartment>()
        tableAuditingService.updateTableAudit<InventoryStock>()

        if (result.first != null)
            return result.first!!
        throw ErrorsResponseException(result.second!!)
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
    @RepeatableReadTransaction
    fun autoMove(slowSellCheck: Boolean = false,
                 event: Boolean = false,
                 autoMove: Boolean = true) : String {

        return autoMoveImpl(slowSellCheck, event, autoMove)
    }

    private fun autoFill() {

        val compartmentsToFill = compartmentCache.apply { checkTables() }
            .findAllByOrderByNumberAscLayerNumberAsc()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() }
            .toHashSet()

        logger.info("Compartments to fill:\n" + compartmentsToFill.map { it.getLocation() }.joinToString("\n", "\n"))

        arrivedPackageCache.checkTables()
        readonlyProductCategorizationRepository.checkTables()
        compartmentProductCategoryCache.checkTables()

        for (order in arrivedPackageCache
            .findAllByOrderByDateTimeDescIdDesc()) {

            val inventoryStocks = inventoryStockCache.apply { checkTables() }
                .findByPackageProductOrderedPackage(order)
                .filter { logic.isExpiring(it.packageProduct).not() }
                .sortedBy { it.inventoryQuantity }

            val inventoryStockToCategoryIDsMap: Map<InventoryStock, Set<String>> = inventoryStocks.associateWith {
                readonlyProductCategorizationRepository
                    .findAllByProduct(it.packageProduct.product)
                    .map { it.category.id }
                    .toSet()
            }

            val categoryIDtoCompartmentsMap: Map<String, List<Compartment>> = compartmentsToFill
                .map {
                    (compartmentProductCategoryCache
                        .findByCompartment(it)
                        ?.productCategory
                        ?.id
                        ?: "*") to it
                }.groupBy ({ it.first },{it.second})

            var newInventoryStocks = inventoryStocks

            for ((categoryID, compartments) in categoryIDtoCompartmentsMap) {
                logger.info("KEY: $categoryID")

                val count = partitionProducts(
                    newInventoryStocks.let {
                        if (categoryID != "*")
                            it.filter {
                                inventoryStockToCategoryIDsMap[it]!!.contains(categoryID)
                            }
                        else
                            it
                    },
                    compartments,
                    updateFunc =
                    {  it.mapNotNull {
                            readonlyInventoryRepository.findById(it.packageProductID!!)
                            .getOrNull()
                        }
                    }
                ).blockingGet()
                logger.info("$categoryID moved count: $count/${compartments.size}")

                newInventoryStocks =
                    newInventoryStocks
                    .mapNotNull {
                        readonlyInventoryRepository.findById(it.packageProductID!!).getOrNull()
                    }.sortedBy { it.inventoryQuantity }
            }

            compartmentsToFill.removeAll(compartmentsToFill
                .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it) }
                .toList().toSet())

            if (compartmentsToFill.isEmpty()) break
        }
    }

    private fun autoMoveImpl(slowSellCheck: Boolean,
                             event: Boolean,
                             autoMove: Boolean) : String {

        eventCompartmentCache.checkTables()
        frontCompartmentCache.checkTables()
        compartmentProductCategoryCache.checkTables()
        compartmentProductCategoryCache.checkTables()
        arrivedPackageCache.checkTables()
        readonlyRowRepository.checkTables()
        compartmentCache.checkTables()
        packageProductCache.checkTables()
        productCache.checkTables()

        removeExpired()

        val slowSellResult =
            if (slowSellCheck)
                arrangeSlowSelling()
            else
                Observable.empty()
        slowSellResult.blockingSubscribe()

        val eventResult =
            if (event)
                arrangeEventStocks()
            else
                Observable.empty()
        eventResult.blockingSubscribe()

        if (autoMove)
            autoFill()

        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )

        return ""
    }

    data class InventoryStockDto(val packageProduct: PackageProduct,
                                var quantity: Int)

    private data class CompartmentPartitionDto(val packageProduct: PackageProduct,
                                       val compartments: List<Compartment>)

    private fun partitionProductCounts(stocks: List<InventoryStockDto>,
                 compartments: List<Compartment>,
                 updateFunc: (List<InventoryStockDto>) -> List<InventoryStockDto> = { listOf() },
                 productOnCompartmentFunc: ProductOnCompartment.() -> Unit = {},
                 entries: List<InventoryStockDto> = stocks,) : Observable<CompartmentPartitionDto> {

        if (entries.isEmpty()) return Observable.empty()

        val entry = entries.first()

        // Fixed: Possible error when size = 1 and stocks.size > 1
        val partitionLimit = Math.ceilDiv(compartments.size, entries.size)

        logger.info("Compartments:")
        compartments.forEach{logger.info(it.getLocation().toString())}

        logger.info("Remaining: ${compartments.size}")
        logger.info("Partition Limit: $partitionLimit")
        logger.info("Product: ${entry.packageProduct.product.sku}")
        logger.info("Quantity: ${entry.quantity}")

        return Observable.just(entry)
            .map {
                val filledCompartments = compartments.subList(0, it.quantity.coerceAtMost(partitionLimit))
                CompartmentPartitionDto(entry.packageProduct, filledCompartments)
            }
            .flatMap {
                //logger.info("Current moved count: $it")

                val count = it.compartments.size

                var nextEntries = entries.subList(1, entries.size)

                val newStocks = stocks.let {
                    if (nextEntries.isEmpty()) {
                        val result = updateFunc(it)
                        nextEntries = result
                        result
                    }
                    else
                        it
                }
                Observable.just(it).let {
                    if (count < compartments.size)
                        it.concatWith(
                            partitionProductCounts(
                                newStocks,
                                compartments.subList(count, compartments.size),
                                updateFunc,
                                productOnCompartmentFunc,
                                nextEntries
                            )
                        )
                    else
                        it
                }
            }
    }

    private fun partitionProducts(stocks: List<InventoryStock>,
                                  compartments: List<Compartment>,
                                  updateFunc: (List<InventoryStock>) -> List<InventoryStock> = { listOf() },
                                  entries: List<InventoryStock> = stocks,
                                  productOnCompartmentFunc: ProductOnCompartment.() -> Unit = {},) : Single<Int> {

        if (entries.isEmpty()) return Observable.just(0).lastOrError()

        val entry = entries.first()

        // Fixed: Possible error when size = 1 and stocks.size > 1
        val partitionLimit = Math.ceilDiv(compartments.size, entries.size)

        logger.info("Compartments:")
        compartments.forEach{logger.info(it.getLocation().toString())}

        logger.info("Remaining: ${compartments.size}")
        logger.info("Partition Limit: $partitionLimit")
        logger.info("Product: ${entry.packageProduct.product.sku}")
        logger.info("Quantity: ${entry.inventoryQuantity}")

        return Observable.just(entry)

            .map {

                val filledCompartments = compartments.subList(0, it.inventoryQuantity.coerceAtMost(partitionLimit))
                shelfAtomicOpsService.moveToShelf(it, filledCompartments)
                filledCompartments.size
            }
            .map {
                //logger.info("Current moved count: $it")

                var nextEntries = entries.subList(1, entries.size)

                val newStocks = stocks.let {
                    if (nextEntries.isEmpty()) {
                        val result = updateFunc(it)
                        nextEntries = result
                        result
                    }
                    else
                        it
                }
                if (it < compartments.size)
                    partitionProducts(
                        newStocks,
                        compartments.subList(it, compartments.size),
                        updateFunc,
                        nextEntries,
                        productOnCompartmentFunc
                    ).blockingGet() + it
                else
                    it
            }.lastOrError()
    }

    @Throws(ErrorsResponseException::class)
    fun move(body: JsonNode) : String {

        logger.info(body.toPrettyString())

        val srcNode = body["src"]
        val desNode = body["des"]

        val srcIterator = Iterable { srcNode.elements() }.map { it.toString() }.iterator()
        val desIterator = Iterable { desNode.elements() }.map { it.toString() }.iterator()

        var errors = ErrorsResponse()

        productCache.checkTables()
        arrivedPackageCache.checkTables()
        packageProductCache.checkTables()

        while (srcIterator.hasNext() && desIterator.hasNext()) {

            val src = srcIterator.next()
            val des = desIterator.next()

            if (des != "null") {

                val desPos: CompartmentPosition

                try {
                    desPos = objectMapper.readValue<CompartmentPosition>(des)
                } catch (ex: DatabindException) {
                    errors = errors.addAsCopy(ErrorsResponse(ex.localizedMessage))
                    continue
                }

                val desResult = logic.getCompartment(desPos)

                if (desResult.first == null) {
                    errors = errors.addAsCopy(desResult.second!!)
                    continue
                }

                val desCompartment = desResult.first!!

                val srcObjNode : JsonNode

                try {
                    srcObjNode = objectMapper.readTree(src)
                } catch (ex: DatabindException) {
                    errors = errors.addAsCopy(ErrorsResponse(ex, HttpStatus.BAD_REQUEST))
                    continue
                }

                try {
                    if (PackageProductItem.matchesJsonNode(srcObjNode)) {

                        val (sku, packageID) = objectMapper.readValue<PackageProductItem>(src)

                        val product = productCache.findById(sku).get()
                        val order = arrivedPackageCache.findById(packageID).get()

                        val packageProduct =
                            packageProductCache.findByOrderedPackageAndProduct(
                                order, product
                            )!!

                        val inventoryStock =
                            readonlyInventoryRepository.findById(packageProduct.id!!).get()

                        shelfAtomicOpsService.moveToShelf(inventoryStock, desCompartment)


                        //errors = errors.addAsCopy(result.second)

                    } else
                        if (CompartmentPosition.matchesJsonNode(srcObjNode)) {

                            val compartmentPosition =
                                objectMapper.readValue<CompartmentPosition>(src)
                            val result = shelfAtomicOpsService.swapStockPlacement(
                                compartmentPosition,
                                desPos
                            )
                            errors = errors.addAsCopy(result)
                        } else errors = errors.addAsCopy(
                            ErrorsResponse(
                                "Malformed Input",
                                HttpStatus.BAD_REQUEST
                            )
                        )
                } catch (ex: Exception) {
                    errors = errors.addAsCopy(ErrorsResponse(ex))
                    continue
                }
            }
            else {

                val srcPos: CompartmentPosition

                try {
                    srcPos = objectMapper.readValue<CompartmentPosition>(src)
                } catch (ex: DatabindException) {
                    errors = errors.addAsCopy(ErrorsResponse(ex.localizedMessage))
                    continue
                }

                val result = shelfAtomicOpsService.moveToInventory(srcPos)

                if (result.first == null) {
                    errors = errors.addAsCopy(result.second!!)
                }
            }
        }

        if (srcIterator.hasNext()) {
            errors = errors.addAsCopy(ErrorsResponse("Source List has ${Iterable { srcIterator }.count()} more items than Destination List"))
        }

        if (desIterator.hasNext()) {
            errors = errors.addAsCopy(ErrorsResponse("Destination List has ${Iterable { desIterator }.count()} more items than Source List"))
        }

        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )

        if (errors.errors.body.any().complement()) {
            return ""
        } else {
            throw ErrorsResponseException(errors)
        }
    }

    @Throws(ErrorsResponseException::class)
    fun moveToShelf(body: MoveProductToShelfRequest): String {

        return moveToShelf(body.src, body.des)
    }

    @Throws(ErrorsResponseException::class)
    private fun moveToShelf(sku: String, compartmentPosition: CompartmentPosition) : String {
        val inventoryStock =
            inventoryStockCache.apply { checkTables() }
                .findAllByOrderByPackageProductExpirationDateDesc()
                .firstOrNull { it.packageProduct.product.sku == sku }
                ?: throw ErrorsResponseException(logic.productNotFoundResponse(sku))

        val compartmentResult = logic.getCompartment(compartmentPosition)
        val compartment = compartmentResult.first
            ?: throw ErrorsResponseException(compartmentResult.second!!)

        val result = shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)
        if (result.first != null)
            return result.first!!
        throw ErrorsResponseException(result.second!!)
    }

    @Throws(ErrorsResponseException::class)
    fun swap(body: Iterable<SwapRequest>) : String {

        val errorsResponse = ErrorsResponse()

        for (request in body) {

            errorsResponse.addAsCopy(shelfAtomicOpsService.swapStockPlacement(request.src, request.des))
        }

        tableAuditingService.updateTableAudit(ProductOnCompartment::class)

        if (errorsResponse.errors.body.any().complement()) {
            return ""
        }

        throw ErrorsResponseException(errorsResponse)
    }

    @Throws(ErrorsResponseException::class)
    fun remove(body: Iterable<CompartmentPosition>) : String {

        val errorsResponse = ErrorsResponse()

        for (request in body) {

            errorsResponse.addAsCopy(shelfAtomicOpsService.moveToInventory(request).second)
        }

        if (errorsResponse.errors.body.any().complement()) {
            return ""
        }

        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )

        throw ErrorsResponseException(errorsResponse)
    }

    @RepeatableReadTransaction
    fun arrangeToFront(productList: ProductList) : String {

        return validateAndArrangeProductList(productList, compartmentCache.apply { checkTables() }
            .findAll())
    }

    @RepeatableReadTransaction
    fun arrangeEventStocks(productList: ProductList) : String {

        return validateAndArrangeProductList(productList, eventCompartmentCache.apply { checkTables() }
            .findAll().map { it.compartment })
    }

    private fun arrangeSlowSelling(): Observable<Unit> {

        val slowSelling = HashMap<PackageProduct, MutableList<Compartment>>()
        val targetCompartments = LinkedList<Compartment>()

        return Observable.fromCallable {
            productOnCompartmentCache.checkTables()
            frontCompartmentCache.checkTables()
            Unit
        }
            .flatMap {
                productOnCompartmentCache.findAll().let { Observable.fromIterable(it) }
                    .map { productOnCompartment ->

                        val packageProduct = productOnCompartment.packageProduct

                        if (frontCompartmentCache.existsByCompartment(productOnCompartment.compartment)
                                .not()
                        ) {
                            if (productOnCompartment.status == 3) {
                                if (slowSelling.contains(packageProduct).not())
                                    slowSelling[packageProduct] = LinkedList()
                                slowSelling[packageProduct]!!.add(productOnCompartment.compartment)
                            }
                        } else {
                            if (productOnCompartment.status == 3)
                                productOnCompartmentRepository.save(
                                    statusDecider.update(productOnCompartment.with(status = 1))
                                )
                            else
                                targetCompartments.add(productOnCompartment.compartment)
                        }
                        Unit
                    }.count()
                    .let {
                        Observable.fromSingle(it)
                    }
                    .flatMap {

                        logger.info("Front Compartments:\n" + slowSelling.map {
                            it.value.joinToString {
                                it.getLocation()
                                    .toString()
                            }
                        }.joinToString("\n", "\n"))

                        partitionProductCounts(
                            slowSelling.entries.toList()
                                .map { InventoryStockDto(it.key, it.value.size) }
                                .sortedBy { it.quantity },
                            targetCompartments,
                            updateFunc = { listOf() }
                        ).map {
                            val srcIterator = slowSelling[it.packageProduct]!!.iterator()
                            val desIterator = it.compartments.iterator()

                            while (srcIterator.hasNext() && desIterator.hasNext()) {
                                val srcCompartment = srcIterator.next()
                                val desCompartment = desIterator.next()

                                shelfAtomicOpsService.checkedSwap(
                                    srcCompartment,
                                    desCompartment
                                )
                            }

                            if (srcIterator.hasNext()) logger.info("Ran out of front compartments.")

                            while (srcIterator.hasNext())
                                shelfAtomicOpsService.moveToInventory(srcIterator.next())

                        }
                    }
            }
    }

    private class InfiniteIterator<T>(private val func: () -> T) : Iterator<T> {
        override fun hasNext() = true

        override fun next() = func()

        constructor(obj: T) : this({obj})
    }

    private fun arrangeEventStocks(): Observable<Unit> {

        val eventStocks = HashMap<PackageProduct, MutableList<Compartment>>()
        val targetCompartments = LinkedList<Compartment>()
        var eventProductSet: Set<Product> = emptySet()

        return Observable.fromCallable {
                eventProductSet = activeEventsService.activeProductsSet

                productOnCompartmentCache.checkTables()
                eventCompartmentCache.checkTables()
            }
            .flatMap {
                productOnCompartmentCache.findAll().let { Observable.fromIterable(it) }
                    .map { productOnCompartment ->
                        val packageProduct = productOnCompartment.packageProduct

                        if (eventCompartmentCache.existsByCompartment(productOnCompartment.compartment)
                                .not()
                        ) {

                            if (productOnCompartment.status == 4) {

                                if (eventStocks.contains(packageProduct).not())
                                    eventStocks[packageProduct] = LinkedList()
                                eventStocks[packageProduct]!!.add(productOnCompartment.compartment)
                            }
                        } else {
                            if (productOnCompartment.status == 4)
                                productOnCompartmentRepository.save(
                                    statusDecider.update(productOnCompartment.with(status = 1))
                                )
                            else
                                if (eventProductSet.contains(
                                        productOnCompartment.packageProduct.product
                                    ).not()
                                )
                                    targetCompartments.add(productOnCompartment.compartment)
                        }
                        Unit
                    }.last(Unit)
                    .let { Observable.fromSingle(it) }
                    .flatMap {

                        logger.info("Event Compartments:\n" + eventStocks.map {
                            it.value.joinToString {
                                it.getLocation()
                                    .toString()
                            }
                        }.joinToString("\n", "\n"))

                        val targetCompartmentSet = targetCompartments.toHashSet()

                        partitionProductCounts(
                            eventStocks.entries.toList()
                                .map { InventoryStockDto(it.key, it.value.size) }
                                .sortedBy { it.quantity },
                            targetCompartments,
                            updateFunc = { listOf() }
                        )
                            .map {
                                val srcIterator = eventStocks[it.packageProduct]!!.iterator()
                                val desIterator = it.compartments.iterator()

                                while (srcIterator.hasNext() && desIterator.hasNext()) {
                                    val srcCompartment = srcIterator.next()
                                    val desCompartment = desIterator.next()

                                    shelfAtomicOpsService.checkedSwap(
                                        srcCompartment,
                                        desCompartment
                                    )

                                    targetCompartmentSet.remove(desCompartment)
                                }
                                if (srcIterator.hasNext()) logger.info("Ran out of front compartments.")

                            }.count().let { Observable.fromSingle(it) }
                            .flatMap {
                                if (targetCompartmentSet.isEmpty())
                                    Observable.empty()
                                else {
                                    Observable.fromIterable(arrivedPackageCache
                                        .apply { checkTables() }
                                        .findAllByOrderByDateTimeDescIdDesc())
                                        .map {
                                            partitionProductCounts(
                                                readonlyInventoryRepository.findByPackageProductOrderedPackage(
                                                    it
                                                )
                                                    .filter {
                                                        eventProductSet.contains(it.packageProduct.product)
                                                                && logic.isExpiring(it.packageProduct)
                                                            .not()
                                                    }.map {
                                                        InventoryStockDto(
                                                            it.packageProduct,
                                                            it.inventoryQuantity
                                                        )
                                                    }
                                                    .sortedBy { it.quantity },
                                                targetCompartmentSet.toList(),
                                                updateFunc = { listOf() }
                                            )
                                                .map {
                                                    val srcIterator =
                                                        eventStocks[it.packageProduct]!!.iterator()
                                                    val desIterator = it.compartments.iterator()

                                                    while (srcIterator.hasNext() && desIterator.hasNext()) {
                                                        val srcCompartment = srcIterator.next()
                                                        val desCompartment = desIterator.next()

                                                        shelfAtomicOpsService.checkedSwap(
                                                            srcCompartment,
                                                            desCompartment
                                                        )

                                                        targetCompartmentSet.remove(desCompartment)
                                                    }
                                                    if (srcIterator.hasNext()) logger.info("Ran out of event compartments while swapping event stocks.")
                                                }.takeUntil {
                                                    targetCompartmentSet.isEmpty().also {
                                                        if (it) logger.info("Ran out of front compartments while placing event stocks.")
                                                    }
                                                }
                                        }.map { Unit }
                                }
                            }
                    }
            }
    }

    @RepeatableReadTransaction
    fun removeExpired() {
        logger.info("Moved all expiring products in compartments back to the inventory.")
        for (compartment in productOnCompartmentCache.apply { checkTables() }.findAll()) {
            if (logic.isExpiring(compartment.packageProduct)) {
                shelfAtomicOpsService.moveToInventory(compartment, false)
            }
        }

        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )
    }

    @RepeatableReadTransaction
    fun removeAll() {
        logger.info("Moved all products in compartments back to the inventory.")

        /*
        Observable.defer {
            Observable.fromIterable(readonlyProductOnCompartmentRepository.findAllId())
        }.subscribeOn(scheduler)
            .map {
                shelfAtomicOpsService.moveToInventory(it)
            }.blockingSubscribe()
         */

        shelfAtomicOpsService.removeAll().blockingSubscribe()

        tableAuditingService.updateTableAudit(
            InventoryStock::class,
            ProductOnCompartment::class
        )
    }


    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartmentsOrAll(rowNumber: Int?) : String {
        return if (rowNumber == null)
            printCompartments()
        else
            printCompartments(rowNumber)
    }


    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartmentsCategoryOrAll(rowNumber: Int?) : String {
        return if (rowNumber == null)
            printCompartmentCategories()
        else
            printCompartmentsCategory(rowNumber)
    }


    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartments() : String {
        return readonlyRowRepository.apply { checkTables() }.findAll()
            .joinToString("\n\n") { "Row ${it.number}:\n" + printCompartments(it.number) }
    }


    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartmentCategories() : String {
        return readonlyRowRepository.apply { checkTables() }.findAll()
            .joinToString("\n\n") { "Row ${it.number}:\n" + printCompartmentsCategory(it.number) }
    }

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartments(rowNumber: Int) : String {

        val layer = readonlyRowRepository.apply { checkTables() }.findByNumber(rowNumber)
            ?: throw ErrorsResponseException(logic.rowNotFoundResponse(rowNumber))

        val emptyText = "______(_)[____]"

        compartmentCache.checkTables()

        val message = blocs.printMap({

            val compartment = compartmentCache.findByLayerAndNumber(layer, it)

            compartment?.let {
                val productOnCompartment = readonlyProductOnCompartmentRepository.findByCompartment(it)
                productOnCompartment?.packageProduct?.product?.sku
                    ?.plus("(${productOnCompartment.status})")
                    ?.plus("[${
                        productOnCompartment.packageProduct.orderedPackage.id!!.toString().let { 
                            "0".repeat((4 - it.length).coerceAtLeast(0)) + it
                        }
                    }]")
            }?: emptyText
        }, " ".repeat(emptyText.length))

        return message
    }

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorsResponseException::class)
    fun printCompartmentsCategory(rowNumber: Int) : String {

        val layer = readonlyRowRepository.apply{ checkTables() }.findByNumber(rowNumber)
            ?: throw ErrorsResponseException(logic.rowNotFoundResponse(rowNumber))

        val emptyText = "__[___]"

        compartmentCache.checkTables()

        return blocs.printMap({

            val compartment = compartmentCache.findByLayerAndNumber(layer, it)

            compartment?.let {
                compartmentProductCategoryCache.findByCompartment(it)
                    ?.productCategory?.id
                    ?.plus("[${
                        it.id!!.toString().let { "0".repeat((3 - it.length).coerceAtLeast(0)) + it }
                    }]")
            }?: emptyText
        }, " ".repeat(emptyText.length))

    }

    @Throws(ErrorsResponseException::class)
    private fun validateAndArrangeProductList(
        productList: ProductList,
        compartments: Iterable<Compartment>
    ): String {
        val list: LinkedList<Product> = LinkedList()

        for (productID in productList.productList) {
            val product = productCache.apply { checkTables() }.findById(productID).getOrNull()
                ?: throw ErrorsResponseException(logic.productNotFoundResponse(productID))
            list.add(product)
        }

        return continuousArrangement(list, compartments)
    }

    private fun continuousArrangement(products: LinkedList<Product>, compartments: Iterable<Compartment>): String {

        //val productsToRearrange = HashMap<Product, int>()

        val stringBuilder = StringBuilder()

        var productIterator = products.iterator()
        val compartmentIterator = compartments.iterator()

        while (productIterator.hasNext() && compartmentIterator.hasNext()) {

            val product = productIterator.next()

            val inventoryStock =
                readonlyInventoryRepository
                    .findAllByOrderByPackageProductExpirationDateDesc()
                    .firstOrNull { it.packageProduct.product == product
                            && it.inventoryQuantity > 0 }

            if (inventoryStock == null) {
                productIterator.remove()
                continue
            }

            val compartment = compartmentIterator.next()

            stringBuilder.appendLine(shelfAtomicOpsService.moveToShelf(inventoryStock, compartment))

            if (productIterator.hasNext().complement()) {
                productIterator = products.iterator()
            }
        }

        return stringBuilder.toString()
    }

    private fun continuousArrangement1(products: LinkedList<Product>, compartments: Iterable<Compartment>) {
        var productIterator = products.iterator()
        val compartmentIterator = compartments.iterator()

        while (productIterator.hasNext() && compartmentIterator.hasNext()) {

            val product = productIterator.next()

            val inventoryStock =
                readonlyInventoryRepository
                    .findAllByOrderByPackageProductExpirationDateDesc()
                    .firstOrNull { it.packageProduct.product == product
                            && it.inventoryQuantity > 0 }

            if (inventoryStock == null) {
                productIterator.remove()
                continue
            }

            val compartment = compartmentIterator.next()

            shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)

            if (productIterator.hasNext().complement()) {
                productIterator = products.iterator()
            }
        }
    }
}