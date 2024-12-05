package server.circlehelp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.request.AppraisalDecisionRequest
import server.circlehelp.api.request.ImportProduct
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.api.response.AppraisalDecisionResponse
import server.circlehelp.api.response.InventoryStockItem
import server.circlehelp.api.response.PersonalSubmittedOrdersResponse
import server.circlehelp.api.response.ProductSpecDto
import server.circlehelp.api.response.SubmittedOrdersResponse
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.entities.Appraisable
import server.circlehelp.entities.OrderSubmissionTopic
import server.circlehelp.repositories.AppraisableRepository
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.OrderSubmissionTopicRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.caches.InventoryStockCache
import server.circlehelp.repositories.caches.ProductCategorizationCache
import server.circlehelp.repositories.readonly.ReadonlyAppraisableRepository
import server.circlehelp.repositories.readonly.ReadonlyImageSourceRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyOrderSubmissionTopicRepository
import server.circlehelp.services.AccountService
import server.circlehelp.services.InventoryService
import server.circlehelp.services.Logic
import java.lang.Math.ceilDiv
import java.math.BigDecimal
import java.time.Clock
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

const val inventory = "/inventory"
const val submitOrder = "/submitOrder"
const val submittedOrders = "/submittedOrders"
@RestController
@ResponseStatus(HttpStatus.OK)
class InventoryController(private val readonlyInventoryRepository: ReadonlyInventoryRepository,
                          private val inventoryStockCache: InventoryStockCache,
                          private val imageSourceRepository: ReadonlyImageSourceRepository,
                          private val productCategorizationCache: ProductCategorizationCache,
                          private val readonlyOrderSubmissionTopicRepository: ReadonlyOrderSubmissionTopicRepository,
                          private val readonlyAppraisableRepository: ReadonlyAppraisableRepository,

                          private val submissionTopicRepository: OrderSubmissionTopicRepository,
                          private val appraisableRepository: AppraisableRepository,
                          private val arrivedPackageRepository: ArrivedPackageRepository,
                          private val packageProductRepository: PackageProductRepository,

                          private val inventoryService: InventoryService,
                          private val accountService: AccountService,

                          private val logic: Logic,
                          private val clock: Clock,
                          objectMapperBuilder: Jackson2ObjectMapperBuilder) {

    private val objectMapper = objectMapperBuilder.build<ObjectMapper>()
    private val logger = LoggerFactory.getLogger(InventoryController::class.java)

    @RepeatableReadTransaction(readOnly = true)
    @GetMapping("$baseURL$inventory")
    fun getInventory(@RequestParam(defaultValue = "") searchTerm: String,
                     @RequestParam(defaultValue = "0") minQuantity: Int,
                     @RequestParam(defaultValue = Int.MAX_VALUE.toString()) maxQuantity: Int,
                     @RequestParam(defaultValue = "0") minPrice: BigDecimal,
                     @RequestParam(defaultValue = Int.MAX_VALUE.toString()) maxPrice: BigDecimal,
                     @RequestParam(defaultValue = "") sortColumn: String,
                     @RequestParam(defaultValue = "") sortOption: String,
                     @RequestParam(defaultValue = "0") page: Int,
                     @RequestParam(defaultValue = "10") pageSize: Int,
                     @RequestParam(defaultValue = "false") onlyExpired: Boolean): PagedModel<InventoryStockItem> {
        return getInventoryImpl(
            searchTerm, minQuantity, maxQuantity, minPrice, maxPrice, sortColumn, sortOption, page, pageSize, onlyExpired,
            true || ( searchTerm.isEmpty() &&
            minQuantity == 0 &&
            maxQuantity == Integer.MAX_VALUE &&
            minPrice == BigDecimal.ZERO &&
            maxPrice == BigDecimal(Int.MAX_VALUE) &&
            sortColumn.isEmpty() &&
            sortOption.isEmpty()).not()
        )
    }

    @RepeatableReadTransaction(readOnly = true)
    @GetMapping("$baseURL$inventory/productSpec")
    fun productSpec(@RequestParam @Size(min = 6, max = 6) sku: String): ProductSpecDto {
        val stocks = inventoryStockCache.apply{ checkTables() }
            .findAllByOrderByPackageProductOrderedPackageDateTimeDescPackageProductOrderedPackageIdDesc()
            .filter { it.packageProduct.product.sku == sku }

        val stock = stocks.first()

        return ProductSpecDto(
            stock.packageProduct.orderedPackage.id!!,
            stock.packageProduct.product.sku,
            stock.packageProduct.product.name,
            stock.packageProduct.product.price,
            stock.packageProduct.wholesalePrice,
            stocks.sumOf { it.inventoryQuantity },
            imageSourceRepository.findAllByProduct(stock.packageProduct.product).first().url,
            productCategorizationCache.apply { checkTables() }
                .findAllByProduct(stock.packageProduct.product).joinToString { it.category.name }
        )
    }

    @RepeatableReadTransaction
    @PostMapping("$baseURL$inventory/importProduct")
    fun importProducts(@RequestBody importProduct: String) {
        logger.info(importProduct)
        inventoryService.importProduct(objectMapper.readValue<ImportProduct>(importProduct))
    }

    @RepeatableReadTransaction
    @PostMapping("$baseURL$inventory$submitOrder")
    fun submitOrder(@RequestBody orderApprovalRequest: OrderApprovalRequest,
                    @CurrentSecurityContext securityContext: SecurityContext): PersonalSubmittedOrdersResponse {

        val submitter = accountService.getUser(securityContext) as Employee

        val appraisable = inventoryService.submitOrder(
            submitter,
            orderApprovalRequest
        )

        return PersonalSubmittedOrdersResponse.fromAppraisable(appraisable)
    }

    @RepeatableReadTransaction(readOnly = true)
    @GetMapping("$baseURL$inventory$submittedOrders")
    fun submittedOrders(@CurrentSecurityContext securityContext: SecurityContext): Iterable<PersonalSubmittedOrdersResponse> {
        return readonlyAppraisableRepository
            .findAll(
                Appraisable.defaultSort)
            .filter { it.submitter.email == accountService.getUser(securityContext)!!.email }
            .filter { it.topic is OrderSubmissionTopic }
            .map(
                PersonalSubmittedOrdersResponse::fromAppraisable
            )
    }

    @RepeatableReadTransaction
    @PostMapping("$admin$baseURL$inventory/appraiseOrder")
    @Throws(IllegalArgumentException::class)
    fun approveOrder(
        @RequestBody appraisalDecisionRequest: AppraisalDecisionRequest,
        @CurrentSecurityContext securityContext: SecurityContext
    ) : AppraisalDecisionResponse {
        val appraiser = accountService.getUser(securityContext) as Admin
        val (id, approved, reason) = appraisalDecisionRequest

        val appraisable = readonlyAppraisableRepository.findById(id).getOrNull()
            ?: throw IllegalArgumentException("No appraisable with id: $id.")

        val topic = (appraisable.topic as? OrderSubmissionTopic)
            ?: throw IllegalArgumentException("Topic is not ${OrderSubmissionTopic::class.simpleName}")

        appraisable.appraise(
            appraiser,
            approved,
            reason
        )

        return AppraisalDecisionResponse(
            appraisalDecisionRequest,
            topic.json
        )
    }

    @RepeatableReadTransaction(readOnly = true)
    @GetMapping("$admin$baseURL$inventory/orderSubmissions")
    fun orderSubmissions() : Iterable<SubmittedOrdersResponse> {
        return readonlyAppraisableRepository.findAll(Appraisable.defaultSort)
            .map(SubmittedOrdersResponse::fromAppraisable)
    }

    private fun getInventoryImpl(
        searchTerm: String,
        minQuantity: Int,
        maxQuantity: Int,
        minPrice: BigDecimal,
        maxPrice: BigDecimal,
        sortColumn: String,
        sortOption: String,
        page: Int,
        pageSize: Int,
        onlyExpired: Boolean,
        appliedFilterOrSort: Boolean): PagedModel<InventoryStockItem> {

        productCategorizationCache.checkTables()

        val inventoryStock = inventoryStockCache.apply { checkTables() }
            .findAllByOrderByPackageProductOrderedPackageDateTimeDescPackageProductOrderedPackageIdDesc()
            .stream()
            .filter {
                (searchTerm.isEmpty() || it.packageProduct.product.name.contains(searchTerm, true))
                        &&
                        (it.packageProduct.product.price in minPrice..maxPrice)
                        &&
                        (it.packageProduct.importedQuantity in minQuantity..maxQuantity)
                        &&
                        (logic.isExpiring(it.packageProduct).complement().xor(onlyExpired)).or(true)
            }

        val result = inventoryStock
            .map {
                InventoryStockItem(
                    it.packageProduct.orderedPackage.id!!,
                    it.packageProduct.product.sku,
                    it.packageProduct.product.name,
                    it.packageProduct.product.price,
                    it.packageProduct.wholesalePrice,
                    it.inventoryQuantity,
                    it.packageProduct.expirationDate,
                    logic.isExpiring(it.packageProduct),
                    it.packageProduct.imageSrc!!.url,
                    productCategorizationCache.apply { checkTables() }
                        .findAllByProduct(it.packageProduct.product).map { it.category.name }
                )
            }

        /*
        val result = inventoryStock
            .collect(groupingBy {it.packageProduct.product.sku})
            .mapValuesTo(HashMap()) {
                val items = it.value
                items.sortBy { it.packageProduct.expirationDate ?: LocalDate.MAX }
                val item = items[0].packageProduct.product
                val packageProduct = items[0].packageProduct
                InventoryStockItem(
                    item.sku,
                    item.name,
                    item.price,
                    packageProduct.wholesalePrice,
                    items.stream().collect(summingInt {
                        if (logic.isExpiring(it.packageProduct))
                            0
                        else
                            it.inventoryQuantity
                    }),
                    packageProduct.expirationDate,
                    imageSourceRepository.findAllByProduct(item).firstOrNull()?.url,
                    productCategorizationRepository.findAllByProduct(item).map { it.category.name }
                )
            }
         */

        val comparator = InventoryStockItem.getComparator(sortColumn).let {
            when (sortOption) {
                "desc" -> it.reversed()
                "asc" -> it
                else -> null
            }
        }

        val body = result.filter { (it.quantity in minQuantity..maxQuantity) }
            .let {
                if (comparator != null)
                    it.sorted(comparator)
                else
                    it
            }

        val sort =
            if (sortColumn.isBlank())
                Sort.unsorted()
            else
                Sort.by(sortColumn)

        //val pageable = PageRequest.of(page, pageSize, sort)
        val list = body.toList()

        val pagedModel = toPage(list, page, pageSize, sort, appliedFilterOrSort)

        return pagedModel
    }

    private class PageReImpl<T>(list: List<T>, pageable: Pageable, val total: Long) : PageImpl<T>(list, pageable, total) {

        private val _pageable: Pageable = pageable

        override fun getTotalElements(): Long {
            return total
        }

        override fun getSize(): Int {
            return _pageable.pageSize
        }

        override fun getTotalPages(): Int {

            return if (size <= 0) 1 else ceilDiv(totalElements, size).toInt()
        }
    }

    private fun <T> toPage(list: List<T>, pageNo: Int, size: Int, sort: Sort,
                           includePreviousPages: Boolean) : PagedModel<T> {
        val pageable = PageRequest.of(min(pageNo, list.size), min(size, list.size), sort)

        return PagedModel(
            PageReImpl(
                list.subList(
                    min(if (includePreviousPages) 0 else pageable.offset.toInt(), list.size),
                    min(pageable.offset.toInt() + pageable.pageSize, list.size)
                ),
                pageable,
                list.size.toLong()
            )
        )
    }


}