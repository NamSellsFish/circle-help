package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.request.ImportProduct
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.entities.Appraisable
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.OrderSubmissionTopic
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.repositories.AppraisableRepository
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.OrderSubmissionTopicRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductCategorizationRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.caches.ArrivedPackageCache
import server.circlehelp.repositories.caches.InventoryStockCache
import server.circlehelp.repositories.caches.PackageProductCache
import server.circlehelp.repositories.caches.ProductCache
import server.circlehelp.repositories.caches.ProductCategorizationCache
import server.circlehelp.repositories.caches.ProductCategoryCache
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.value_classes.UrlValue
import java.time.Clock
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

//TODO: Update inventory stock table audit
@Service
class InventoryService(
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val inventoryStockCache: InventoryStockCache,
    private val readonlyPackageProductRepository: PackageProductCache,
    private val arrivedPackageCache: ArrivedPackageCache,
    private val productCache: ProductCache,
    private val productCategorizationCache: ProductCategorizationCache,
    private val categoryCache: ProductCategoryCache,
    private val readonlyProductCategorizationRepository: ReadonlyProductCategorizationRepository,

    private val inventoryRepository: InventoryRepository,
    private val packageProductRepository: PackageProductRepository,
    private val arrivedPackageRepository: ArrivedPackageRepository,
    private val orderSubmissionTopicRepository: OrderSubmissionTopicRepository,
    private val appraisableRepository: AppraisableRepository,
    private val productRepository: ProductRepository,
    private val productCategorizationRepository: ProductCategorizationRepository,

    private val clock: Clock,
    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val tableAuditingService: TableAuditingService,
    private val entityManager: EntityManager
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()
    private val logger = LoggerFactory.getLogger(InventoryService::class.java)

    @RepeatableReadTransaction
    fun importProduct(importProduct: ImportProduct) {

        val arrivedPackage = arrivedPackageCache.apply { checkTables() }
            .findById(importProduct.packageID).getOrNull().apply { logger.info("Order ${if (this==null) "not" else ""} found.") }
            ?: arrivedPackageRepository.save(
                ArrivedPackage(importProduct.supplier, LocalDateTime.now(clock), importProduct.packageID)
            )

        val product = productCache.apply { checkTables() }
            .findById(importProduct.sku).getOrNull().apply { logger.info("Product ${if (this==null) "not" else ""} found.") }
            ?: productRepository.save(Product(importProduct.sku, importProduct.name, importProduct.price))

        importProduct.categories.map {
            categoryCache.apply { checkTables() }.findByName(it)
                ?: throw NoSuchElementException("No category with name '$it'.")
        }.map {
             if (readonlyProductCategorizationRepository.existsByProductAndCategory(product, it).not())
                productCategorizationRepository.save(ProductCategorization(product, it))
        }

        val packageProduct = readonlyPackageProductRepository.apply { checkTables() }
            .findByOrderedPackageAndProduct(arrivedPackage, product)
            .let {
                if (it == null) {
                    logger.info("PackageProduct not found.")
                    packageProductRepository.save(
                        PackageProduct(
                            arrivedPackage,
                            product,
                            importProduct.quantity,
                            importProduct.wholesalePrice,
                            importProduct.expirationDate,
                            UrlValue(importProduct.imageUrl),
                            importProduct.note.orEmpty()
                        )
                    )
                } else {
                    logger.info("PackageProduct found.")
                    packageProductRepository.save(
                        it.apply { logger.info("Old Quantity: ${importedQuantity}") }
                            .apply {
                            importedQuantity += importProduct.quantity
                            note = importProduct.note.orEmpty()
                            imageSrc = UrlValue(importProduct.imageUrl)
                        }.apply { logger.info("New Quantity: ${importedQuantity}") }
                    )
                }
            }

        val inventoryStock = inventoryStockCache.apply { checkTables() }
            .findByPackageProduct(packageProduct)
            .let {
                if (it == null) {
                    logger.info("InventoryStock not found.")
                    inventoryRepository.save(
                        InventoryStock(
                            packageProduct,
                            importProduct.quantity
                        )
                    )
                } else {
                    logger.info("InventoryStock found.")
                    inventoryRepository.save(it.apply { logger.info("Old Quantity: ${inventoryQuantity}") }
                        .apply {inventoryQuantity += importProduct.quantity}
                        .apply { logger.info("New Quantity: ${inventoryQuantity}") })
                }
            }

        tableAuditingService.updateTableAudit(InventoryStock::class)
    }

    @RepeatableReadTransaction
    fun submitOrder(submitter: Employee, request: OrderApprovalRequest): Appraisable {

        val topic = orderSubmissionTopicRepository.save(
            OrderSubmissionTopic(
                objectMapper.writeValueAsString(request)
            )
        )

        val appraisable = appraisableRepository.save(
            Appraisable(
                submitter,
                topic
            )
        )

        return appraisable
    }

    @RepeatableReadTransaction
    fun appraiseOrder(appraiser: Admin, appraisable: Appraisable, approved: Boolean, reason: String = "") {
        appraisable.appraise(appraiser, approved, reason)
    }

    @RepeatableReadTransaction
    fun addOrder(orderApprovalRequest: OrderApprovalRequest): ArrivedPackage {

        val order = arrivedPackageRepository.save(orderApprovalRequest.toArrivedPackage())

        for (it in orderApprovalRequest.packageProducts) {

            val product = productCache.findById(it.sku).get()
            val packageProduct = packageProductRepository.save(it.toPackageProduct(order, product))
            val inventoryStock = inventoryRepository.save(InventoryStock.import(packageProduct))
        }

        return order
    }
}