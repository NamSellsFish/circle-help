package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.caches.CacheRepositoryImpl.Companion.getOrPutIfNotNull
import server.circlehelp.repositories.readonly.ReadonlyProductCategoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class ProductCategoryCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<ProductCategory, String>,
    private val cacheManager: CacheManager,
    private val readonlyProductCategoryRepository: ReadonlyProductCategoryRepository,
): ReadonlyProductCategoryRepository, CacheRepository<ProductCategory, String> by cacheRepository {

    @Autowired
    constructor(
        readonlyProductCategoryRepository: ReadonlyProductCategoryRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                tableName,
                { it.id },
                readonlyProductCategoryRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }, cacheManager, readonlyProductCategoryRepository
    )

    init {
        cacheRepository.updateEvent = ::clear
    }

    private fun clear() {
        nameCache.invalidate()
    }

    private val nameCache: Cache
        get() = cacheManager.getCache("${tableName}.name")!!


    override fun findByName(name: String): ProductCategory? {
        return nameCache.get(name) { readonlyProductCategoryRepository.findByName(name) }
    }

    companion object {
        private val tableName = TableAudit.toSnakeCase<ProductCategory>()
    }
}