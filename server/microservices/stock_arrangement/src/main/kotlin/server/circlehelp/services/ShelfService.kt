package server.circlehelp.services

import com.fasterxml.jackson.databind.DatabindException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
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
import server.circlehelp.api.response.ErrorResponse
import server.circlehelp.api.response.ErrorResponseException
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
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.readonly.ReadonlyArrivedPackageRepository
import server.circlehelp.repositories.readonly.ReadonlyCompartmentProductCategoryRepository
import server.circlehelp.repositories.readonly.ReadonlyCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyEventCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyEventProductRepository
import server.circlehelp.repositories.readonly.ReadonlyFrontCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyRowRepository
import java.util.HashMap
import java.util.LinkedList
import kotlin.jvm.Throws
import kotlin.jvm.optionals.getOrNull

@Service
@RepeatableReadTransaction
class ShelfService(
    private val productRepository: ProductRepository,
    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyRowRepository: ReadonlyRowRepository,
    private val readonlyCompartmentRepository: ReadonlyCompartmentRepository,
    private val readonlyPackageProductRepository: ReadonlyPackageProductRepository,
    private val readonlyFrontCompartmentRepository: ReadonlyFrontCompartmentRepository,
    private val readonlyEventCompartmentRepository: ReadonlyEventCompartmentRepository,
    private val readonlyCompartmentProductCategoryRepository: ReadonlyCompartmentProductCategoryRepository,
    private val readonlyProductCategorizationRepository: ReadonlyProductCategorizationRepository,
    private val readonlyArrivedPackageRepository: ReadonlyArrivedPackageRepository,
    private val readonlyEventProductRepository: ReadonlyEventProductRepository,

    private val inventoryRepository: InventoryRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val responseBodyWriter: ResponseBodyWriter,
    private val logic: Logic,
    private val shelfAtomicOpsService: ShelfAtomicOpsService,
    private val blocs: Blocs,

    @Qualifier(BeanQualifiers.computationScheduler) private val computationScheduler: Scheduler,
    @Qualifier(BeanQualifiers.sameThreadScheduler) private val sameThreadScheduler: Scheduler,
    private val entityManager: EntityManager,
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()
    private val logger = LoggerFactory.getLogger(ShelfService::class.java)

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorResponseException::class)
    fun getStock(
        rowNumber: Int,
        compartmentNumber: Int
    ): ProductDetails? {

        Schedulers.computation()

        val compartmentResult =
            logic.getCompartment(
                CompartmentPosition(rowNumber, compartmentNumber)
            )

        val compartment = compartmentResult.first
            ?: throw ErrorResponseException(compartmentResult.second!!)

        val productOnCompartment =
            readonlyProductOnCompartmentRepository.findByCompartment(compartment)
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

    @RepeatableReadTransaction(readOnly = false)
    fun getStocks(rowNumber: Int) : List<ProductOnCompartmentDto> {

        return getStocks2(rowNumber)
    }

    private fun getStocks1(rowNumber: Int): List<ProductOnCompartmentDto> {
        return readonlyProductOnCompartmentRepository
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
                    readonlyCompartmentProductCategoryRepository.findByCompartment(it.compartment)
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
        return readonlyCompartmentRepository
            .findAll()
            .filter { it.layer.number == rowNumber }
            .map {

                val productOnCompartment = readonlyProductOnCompartmentRepository.findByCompartment(it)

                if (productOnCompartment != null)
                    logic.updateExpiration(productOnCompartment)

                val order = productOnCompartment?.packageProduct

                val productDetails : ProductDetails? = if (order != null) {
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
                    readonlyCompartmentProductCategoryRepository.findByCompartment(it)
                        ?.productCategory?.name ?: "",
                    productDetails
                )
            }
    }

    @Throws(ErrorResponseException::class)
    fun autoMove1(productID: ProductID) : String {
        val compartment = readonlyCompartmentRepository
            .findAll()
            .firstOrNull { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement()  }
            ?: return "No empty compartments found."

        val inventoryStock = readonlyInventoryRepository
            .findAllByOrderByPackageProductExpirationDateDesc()
            .firstOrNull { it.packageProduct.product.sku == productID.sku }
            ?: throw ErrorResponseException(
                logic.notInInventoryResponse(productID.sku))

        val result = shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)

        if (result.first != null)
            return result.first!!
        throw ErrorResponseException(result.second!!)
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
    fun autoMove(slowSellCheck: Boolean = false,
                 event: Boolean = false,
                 autoMove: Boolean = true) : String {

        return autoMoveImpl(slowSellCheck, event, autoMove)
    }

    private fun autoMoveImpl(slowSellCheck: Boolean,
                             event: Boolean,
                             autoMove: Boolean) : String {

        val compartmentsToFill = HashSet<Compartment>()
        val frontCompartments = readonlyFrontCompartmentRepository
            .findAll().map { it.compartment }

        val slowSelling = HashMap<PackageProduct, InventoryStock>()

        if (slowSellCheck) {
            for (productOnCompartment in readonlyProductOnCompartmentRepository.findAllByStatus(3)) {
                if (frontCompartments.isEmpty()) break

                //compartmentsToFill.add(productOnCompartment.compartment)

                //shelfAtomicOpsService.swapStockPlacement(frontCompartments[0], productOnCompartment.compartment)

                val packageProduct = productOnCompartment.packageProduct

                slowSelling[packageProduct] =
                    (slowSelling[packageProduct]
                        ?: InventoryStock(packageProduct))
                        .apply { inventoryQuantity++ }

                shelfAtomicOpsService.moveToInventory(productOnCompartment)
            }

            partitionProducts(
                slowSelling.values.toList().sortedBy { it.inventoryQuantity } ,
                frontCompartments,
                updateFunc =  { listOf() }
            )
        }

        for (productOnCompartment in readonlyProductOnCompartmentRepository
            .findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc()) {
            if (logic.isExpiring(productOnCompartment.packageProduct)) {
                //compartmentsToFill.add(productOnCompartment.compartment)
                shelfAtomicOpsService.moveToInventory(productOnCompartment)
            }
        }

        if (event) {
            val eventProductSet = logic.activeEventProducts().map { it.product }.toHashSet()
            partitionProducts(
                readonlyInventoryRepository.findAllByOrderByPackageProductExpirationDateDesc().filter {
                    eventProductSet.contains(it.packageProduct.product)
                }.sortedBy { it.inventoryQuantity },
                readonlyEventCompartmentRepository.findAll().map { it.compartment },
                updateFunc = { listOf() }
            )
        }

        if (autoMove.not()) return ""

        if (true) compartmentsToFill.addAll(readonlyCompartmentRepository
            .findAllByOrderByNumberAscLayerNumberAsc()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() })

        for (order in readonlyArrivedPackageRepository.findAllByOrderByDateDescIdDesc()) {

            val inventoryStocks = readonlyPackageProductRepository
                .findAllByOrderedPackage(order)
                .mapNotNull { readonlyInventoryRepository.findByPackageProduct(it) }
                .associateWith { it.inventoryQuantity }

            val categoryCompartments = compartmentsToFill
                .map { readonlyCompartmentProductCategoryRepository.findByCompartment(it) }
                .groupBy { it?.productCategory?.id?: "*" }
                .mapValues { it.value.count() }

            val productCategorySets = inventoryStocks.mapValues {
                readonlyProductCategorizationRepository
                    .findAllByProduct(it.key.packageProduct.product)
                    .map { it.category.id }
                    .toHashSet()
            }

            /*
            val mutableInventoryStocks = inventoryStocks.toMutableMap()
            val mutableCategoryCompartments = categoryCompartments.toMutableMap()
            val productSet = mutableInventoryStocks.keys
            val categorySet = mutableCategoryCompartments.key
            //logger.info(productCategorySets.entries.joinToString())
            var productIterator = productSet.iterator()
            var categoryIterator = categorySet.iterator()
            var progressMade = false
             */

            val counterMap = categoryCompartments.mapValues {
                null as Map<InventoryStock, Int>?
            }.toMutableMap()

            // Side-by-side implementation
            /*
            while (productIterator.hasNext() && categoryIterator.hasNext()) {

                val stock = productIterator.next()
                val categoryID = categoryIterator.next()

                if (categoryID == "*"
                    || productCategorySets[stock]!!.contains(categoryID)) {

                    mutableInventoryStocks[stock] =
                        mutableInventoryStocks[stock]!! - 1
                    mutableCategoryCompartments[categoryID] =
                        mutableCategoryCompartments[categoryID]!! - 1

                    if (mutableInventoryStocks[stock]!! <= 0)
                        productIterator.remove()

                    if (mutableCategoryCompartments[categoryID]!! <= 0)
                        categoryIterator.remove()

                    counterMap[categoryID]!![stock] =
                        counterMap[categoryID]!![stock]!! + 1

                    progressMade = true
                }
                if (! productIterator.hasNext()) {
                    if (! progressMade) break
                    productIterator = productSet.iterator()
                    progressMade = false
                }
                if (! categoryIterator.hasNext()) {
                    if (! progressMade) break
                    categoryIterator = categorySet.iterator()
                    progressMade = false
                }
            }
             */

            /*
            for (categoryID in categorySet) {
                while (productIterator.hasNext()) {

                    val stock = productIterator.next()


                    if (categoryID == "*"
                        || productCategorySets[stock]!!.contains(categoryID)
                    ) {

                        mutableInventoryStocks[stock] =
                            mutableInventoryStocks[stock]!! - 1
                        mutableCategoryCompartments[categoryID] =
                            mutableCategoryCompartments[categoryID]!! - 1

                        if (mutableInventoryStocks[stock]!! <= 0)
                            productIterator.remove()

                        if (mutableCategoryCompartments[categoryID]!! <= 0)
                            break

                        counterMap[categoryID]!![stock] =
                            counterMap[categoryID]!![stock]!! + 1

                        progressMade = true
                    }
                    if (!productIterator.hasNext()) {
                        if (!progressMade) break
                        productIterator = productSet.iterator()
                        progressMade = false
                    }
                }

                if (!progressMade) break
                progressMade = false
            }
             */

            /*
            var inventoryStockCounts = inventoryStocks

            for ((categoryID, count) in categoryCompartments) {
                val result = partitionProductCounts(inventoryStockCounts.filterKeys {
                    categoryID == "*" ||
                    productCategorySets[it]!!.contains(categoryID)
                }, count)

                counterMap[categoryID] = result

                inventoryStockCounts = inventoryStockCounts
                    .mapValues { it.value - (result[it.key]?: 0) }
                    .filterValues { it != 0 }
            }
             */

            val compartmentGroups = compartmentsToFill
                .map {
                    (readonlyCompartmentProductCategoryRepository
                        .findByCompartment(it)
                        ?.productCategory
                        ?.id
                        ?: "*") to it
                }.groupBy ({ it.first },{it.second})
            //.mapValues { it.value.toHashSet() }

            //println("counterMapKeys: ${counterMap.keys.joinToString()}")
            //println("compartmentGroupsKey: ${compartmentGroups.keys.joinToString()}")

            var inventoryStockCounts = inventoryStocks.keys.toList().sortedBy { it.inventoryQuantity }

            for ((key, collection) in compartmentGroups) {
                logger.info("KEY: $key")
                if (counterMap.containsKey(key)) {
                    val count = partitionProducts(inventoryStockCounts.filter {
                        key == "*" ||
                                productCategorySets[it]!!.contains(key)
                    },
                    collection,
                    updateFunc =
                    {  it.mapNotNull {
                        readonlyInventoryRepository.findById(it.id!!)
                            .getOrNull()
                        }
                    }
                    ).blockingGet()
                    logger.info("$key moved count: $count/${collection.size}")
                }

                inventoryStockCounts = inventoryStockCounts.mapNotNull {
                    readonlyInventoryRepository.findById(it.id!!).getOrNull()
                }.sortedBy { it.inventoryQuantity }
            }

            /*
            for (inventoryStock in inventoryStocks) {
                if (inventoryStock == null) continue

                var quantity = inventoryStock.inventoryQuantity

                val filteredCompartments = compartmentsToFill.filter {
                    val compartmentCategory =
                        compartmentProductCategoryRepository
                            .findByCompartment(it)

                    val value : Boolean
                    if (compartmentCategory != null)
                        value = readonlyProductCategorizationRepository.findAllByProduct(inventoryStock.packageProduct.product)
                            .map { it.category }
                            .contains(compartmentCategory.productCategory)
                    else
                        value = true
                    value
                }

                for (compartment in filteredCompartments) {

                    shelfAtomicOpsService.moveToShelf(inventoryStock, compartment, true)
                    quantity--

                    compartmentsToFill.remove(compartment)
                    if (quantity == 0) break
                }
                if (compartmentsToFill.isEmpty()) break
            }
             */

            compartmentsToFill.removeAll(compartmentsToFill
                .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it) }
                .toList().toSet())

            if (compartmentsToFill.isEmpty()) break

            shelfAtomicOpsService.compartments.clear()
        }

        return ""
    }

    private fun partitionProductCounts(stocks: Map<InventoryStock, Int>,
                                       compartmentCount: Int,
                                       newMap: MutableMap<InventoryStock, Int> = stocks.mapValues{0}.toMutableMap()
    ) : Map<InventoryStock, Int> {


        if (compartmentCount == 0 || stocks.isEmpty()) return stocks

        /*
        logger.info(stocks.entries.joinToString())
        logger.info(compartmentCount.toString())
        logger.info(newMap.entries.joinToString())

         */

        var counter = compartmentCount

        val partitionLimit = (compartmentCount / stocks.size).coerceAtLeast(1)

        for ((inventoryStock, count) in stocks) {
            for (i in 1..(count - newMap[inventoryStock]!!).coerceAtMost(partitionLimit)) {
                if (counter == 0) return newMap
                if (newMap[inventoryStock] == stocks[inventoryStock]) {
                    logger.warn("Limit met.")
                    break
                }
                counter--
                newMap[inventoryStock] = newMap[inventoryStock]!! + 1
            }
        }

        return partitionProductCounts(stocks.filter { it.value != newMap[it.key] }, counter, newMap)
    }

    /**
     * TODO: Algorithm may complete after all products have been processed. [updateFunc] may no longer be necessary.
     */
    private fun partitionProducts(stocks: List<InventoryStock>,
                                  compartments: List<Compartment>,
                                  entries: List<InventoryStock> = stocks,
                                  updateFunc: (List<InventoryStock>) -> List<InventoryStock>) : Single<Int> {

        if (entries.isEmpty()) return Observable.just(0).lastOrError()

        val entry = entries.first()

        // Fixed: Possible error when size = 1 and stocks.size > 1
        val partitionLimit = Math.ceilDiv(compartments.size, entries.size)

        /*
        logger.info("Compartments:")
        compartments.forEach{logger.info(it.getLocation().toString())}

        logger.info("Remaining: ${compartments.size}")
        logger.info("Partition Limit: $partitionLimit")
        logger.info("Product: ${entry.packageProduct.product.sku}")
        logger.info("Quantity: ${entry.inventoryQuantity}")
        */

        return Observable.just(entry)

            .map {

                val filledCompartments = compartments.subList(0, it.inventoryQuantity.coerceAtMost(partitionLimit))
                shelfAtomicOpsService.moveToShelf(it, filledCompartments)
                filledCompartments.size
            }
            .map {
                logger.info("Current moved count: $it")

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
                        nextEntries,
                        updateFunc
                    ).blockingGet() + it
                else
                    it
            }.lastOrError()
    }

    @Throws(ErrorResponseException::class)
    fun move(body: JsonNode) : String {

        logger.info(body.toPrettyString())

        val srcNode = body["src"]
        val desNode = body["des"]

        val srcIterator = Iterable { srcNode.elements() }.map { it.toString() }.iterator()
        val desIterator = Iterable { desNode.elements() }.map { it.toString() }.iterator()

        var errors = ErrorResponse()

        while (srcIterator.hasNext() && desIterator.hasNext()) {

            val src = srcIterator.next()
            val des = desIterator.next()

            if (des != "null") {

                val desPos: CompartmentPosition

                try {
                    desPos = objectMapper.readValue<CompartmentPosition>(des)
                } catch (ex: DatabindException) {
                    errors = errors.addAsCopy(ErrorResponse(ex.localizedMessage))
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
                    errors = errors.addAsCopy(ErrorResponse(ex, HttpStatus.BAD_REQUEST))
                    continue
                }

                try {
                    if (PackageProductItem.matchesJsonNode(srcObjNode)) {

                        val (sku, packageID) = objectMapper.readValue<PackageProductItem>(src)

                        val product = productRepository.findById(sku).get()
                        val order = readonlyArrivedPackageRepository.findById(packageID).get()

                        val packageProduct =
                            readonlyPackageProductRepository.findByOrderedPackageAndProduct(
                                order, product
                            )!!

                        val inventoryStock =
                            readonlyInventoryRepository.findByPackageProduct(packageProduct)!!

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
                            ErrorResponse(
                                "Malformed Input",
                                HttpStatus.BAD_REQUEST
                            )
                        )
                } catch (ex: Exception) {
                    errors = errors.addAsCopy(ErrorResponse(ex))
                    continue
                }
            }
            else {

                val srcPos: CompartmentPosition

                try {
                    srcPos = objectMapper.readValue<CompartmentPosition>(src)
                } catch (ex: DatabindException) {
                    errors = errors.addAsCopy(ErrorResponse(ex.localizedMessage))
                    continue
                }

                val result = shelfAtomicOpsService.moveToInventory(srcPos)

                if (result.first == null) {
                    errors = errors.addAsCopy(result.second!!)
                }
            }
        }

        if (srcIterator.hasNext()) {
            errors = errors.addAsCopy(ErrorResponse("Source List has ${Iterable { srcIterator }.count()} more items than Destination List"))
        }

        if (desIterator.hasNext()) {
            errors = errors.addAsCopy(ErrorResponse("Destination List has ${Iterable { desIterator }.count()} more items than Source List"))
        }

        if (errors.errors.body.any().complement()) {
            return ""
        } else {
            throw ErrorResponseException(errors)
        }
    }

    @Throws(ErrorResponseException::class)
    fun moveToShelf(body: MoveProductToShelfRequest): String {

        return moveToShelf(body.src, body.des)
    }

    @Throws(ErrorResponseException::class)
    private fun moveToShelf(sku: String, compartmentPosition: CompartmentPosition) : String {
        val inventoryStock =
            readonlyInventoryRepository
                .findAllByOrderByPackageProductExpirationDateDesc()
                .firstOrNull { it.packageProduct.product.sku == sku }
                ?: throw ErrorResponseException(logic.productNotFoundResponse(sku))

        val compartmentResult = logic.getCompartment(compartmentPosition)
        val compartment = compartmentResult.first
            ?: throw ErrorResponseException(compartmentResult.second!!)

        val result = shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)
        if (result.first != null)
            return result.first!!
        throw ErrorResponseException(result.second!!)
    }

    @Throws(ErrorResponseException::class)
    fun swap(body: Iterable<SwapRequest>) : String {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.addAsCopy(shelfAtomicOpsService.swapStockPlacement(request.src, request.des))
        }

        if (errorResponse.errors.body.any().complement()) {
            return ""
        }

        throw ErrorResponseException(errorResponse)
    }

    @Throws(ErrorResponseException::class)
    fun remove(body: Iterable<CompartmentPosition>) : String {

        val errorResponse = ErrorResponse()

        for (request in body) {

            errorResponse.addAsCopy(shelfAtomicOpsService.moveToInventory(request).second)
        }

        if (errorResponse.errors.body.any().complement()) {
            return ""
        }

        throw ErrorResponseException(errorResponse)
    }

    fun arrangeToFront(productList: ProductList) : String {

        return validateAndArrangeProductList(productList, readonlyCompartmentRepository.findAll())
    }

    fun arrangeEventStocks(productList: ProductList) : String {

        return validateAndArrangeProductList(productList, readonlyEventCompartmentRepository.findAll().map { it.compartment })
    }

    fun removeExpired() {
        logger.info("Moved all expiring products in compartments back to the inventory.")
        for (compartment in readonlyProductOnCompartmentRepository.findAll()) {
            if (logic.isExpiring(compartment.packageProduct)) {
                shelfAtomicOpsService.moveToInventory(compartment, false)
            }
        }
    }

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
    }

    fun printCompartmentsOrAll(rowNumber: Int?) : String {
        return if (rowNumber == null)
            printCompartments()
        else
            printCompartments(rowNumber)
    }

    fun printCompartmentsCategoryOrAll(rowNumber: Int?) : String {
        return if (rowNumber == null)
            printCompartmentCategories()
        else
            printCompartmentsCategory(rowNumber)
    }

    fun printCompartments() : String {
        return readonlyRowRepository.findAll()
            .joinToString("\n\n") { "Row ${it.number}:\n" + printCompartments(it.number) }
    }

    fun printCompartmentCategories() : String {
        return readonlyRowRepository.findAll()
            .joinToString("\n\n") { "Row ${it.number}:\n" + printCompartmentsCategory(it.number) }
    }

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorResponseException::class)
    fun printCompartments(rowNumber: Int) : String {

        val layer = readonlyRowRepository.findByNumber(rowNumber)
            ?: throw ErrorResponseException(logic.rowNotFoundResponse(rowNumber))

        val emptyText = "______(_)"

        val message = blocs.printMap({

            val compartment = readonlyCompartmentRepository.findByLayerAndNumber(layer, it)

            compartment?.let {
                val productOnCompartment = readonlyProductOnCompartmentRepository.findByCompartment(it)
                productOnCompartment?.packageProduct?.product?.sku
                    ?.plus("(${productOnCompartment.status})")
            }?: emptyText
        }, " ".repeat(emptyText.length))

        return message
    }

    @RepeatableReadTransaction(readOnly = true)
    @Throws(ErrorResponseException::class)
    fun printCompartmentsCategory(rowNumber: Int) : String {

        val layer = readonlyRowRepository.findByNumber(rowNumber)
            ?: throw ErrorResponseException(logic.rowNotFoundResponse(rowNumber))

        return blocs.printMap({

            val compartment = readonlyCompartmentRepository.findByLayerAndNumber(layer, it)

            compartment?.let {
                readonlyCompartmentProductCategoryRepository.findByCompartment(it)
                    ?.productCategory?.id
            }?: "__"
        }, "  ")

    }

    @Throws(ErrorResponseException::class)
    private fun validateAndArrangeProductList(
        productList: ProductList,
        compartments: Iterable<Compartment>
    ): String {
        val list: LinkedList<Product> = LinkedList()

        for (productID in productList.productList) {
            val product = productRepository.findById(productID).getOrNull()
                ?: throw ErrorResponseException(logic.productNotFoundResponse(productID))
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