package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Layer
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyRowRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class LayerCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<Layer, Long>,
    private val readonlyRowRepository: ReadonlyRowRepository,
    private val cacheManager: CacheManager,
): ReadonlyRowRepository, CacheRepository<Layer, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyRowRepository: ReadonlyRowRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<Layer>(),
                { it.id!! },
                readonlyRowRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }, readonlyRowRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() {
        numberCache.invalidate()
    }

    val tableName = TableAudit.toSnakeCase<Layer>()

    private val numberCache: Cache
        get() = cacheManager.getCache("${tableName}.number")!!

    override fun findByNumber(number: Int): Layer? {
        return numberCache.get(number,
            {readonlyRowRepository.findByNumber(number)})
    }
}