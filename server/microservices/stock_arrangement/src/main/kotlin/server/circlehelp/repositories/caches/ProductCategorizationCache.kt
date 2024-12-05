package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.delegated_classes.DependencyNode
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.caches.CacheRepositoryImpl.Companion.getOrPutIfNotNull
import server.circlehelp.repositories.readonly.ReadonlyProductCategorizationRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class ProductCategorizationCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<ProductCategorization, Long>,
    private val readonlyProductCategorizationRepository: ReadonlyProductCategorizationRepository,
    private val cacheManager: CacheManager,
): ReadonlyProductCategorizationRepository, CacheRepository<ProductCategorization, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyProductCategorizationRepository: ReadonlyProductCategorizationRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager,
        @Qualifier("productCategorizationDependencies") productCategorizationDependencies: DependencyNode
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<ProductCategorization>(),
                { it.id!! },
                readonlyProductCategorizationRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager,
                productCategorizationDependencies.streamAll().map { TableAudit.toSnakeCase(it.item.simpleName!!) }.toList().let { it.takeLast(it.size - 1) }
            )
        }, readonlyProductCategorizationRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() {
        categoryCache.invalidate()
        productCache.invalidate()
        productSetCache.invalidate()
    }

    val tableName = TableAudit.toSnakeCase<ProductCategorization>()

    private val categoryCache: Cache
        get() = cacheManager.getCache("${tableName}.category")!!

    private val productCache: Cache
        get() = cacheManager.getCache("${tableName}.product")!!

    private val productSetCache: Cache
        get() = cacheManager.getCache("${tableName}.productSet")!!

    private val productCategorizationCache: Cache
        get() = cacheManager.getCache("${tableName}.productCategorization")!!

    override fun findAllByProduct(product: Product): List<ProductCategorization> {
        return productCache.get(product,
            {readonlyProductCategorizationRepository.findAllByProduct(product)})!!
    }

    fun findAllByProductAsProductCategorySet(product: Product): Set<ProductCategory> {
        return productSetCache.get(product,
            { readonlyProductCategorizationRepository.findAllByProduct(product).map { it.category }.toSet() })!!
    }

        override fun findAllByCategory(category: ProductCategory): List<ProductCategorization> {
        return categoryCache.get(category,
            {readonlyProductCategorizationRepository.findAllByCategory(category)})!!
    }

    override fun findByProductAndCategory(
        product: Product,
        category: ProductCategory
    ): ProductCategorization? {
        return productCategorizationCache.get(SimpleKeyGenerator.generateKey(product, category)) {
            readonlyProductCategorizationRepository.findByProductAndCategory(product, category)
        }
    }

    override fun existsByProductAndCategory(product: Product, category: ProductCategory): Boolean {
        return findByProductAndCategory(product, category) != null
    }

}