package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyArrivedPackageRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class ArrivedPackageCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<ArrivedPackage, Long>
): ReadonlyArrivedPackageRepository, CacheRepository<ArrivedPackage, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyArrivedPackageRepository: ReadonlyArrivedPackageRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager,
    ) : this(
        transactionService.requiredRollbackOnAny{
        CacheRepositoryImpl(
            TableAudit.toSnakeCase<ArrivedPackage>(),
            { it.id!! },
            readonlyArrivedPackageRepository,
            tableWatcherBuilderService,
            cacheManager,
            entityManager
        )}
    )

    override fun findAllByOrderByDateTimeDescIdDesc(): List<ArrivedPackage> {
        return findAll(ArrivedPackage.defaultSort)
    }
}