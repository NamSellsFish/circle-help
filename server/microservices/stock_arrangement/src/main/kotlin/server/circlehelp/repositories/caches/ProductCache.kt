package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Product
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyProductRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class ProductCache protected constructor(
    private val cacheRepository: CacheRepository<Product, String>,
): ReadonlyProductRepository, CacheRepository<Product, String> by cacheRepository {

    @Autowired
    constructor(
        readonlyProductRepository: ReadonlyProductRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl<Product, String>(
                TableAudit.toSnakeCase<Product>(),
                { it.sku },
                readonlyProductRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }
    )
}