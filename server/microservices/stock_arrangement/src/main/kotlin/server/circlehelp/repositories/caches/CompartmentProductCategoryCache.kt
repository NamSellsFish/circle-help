package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyCompartmentProductCategoryRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService
import kotlin.jvm.optionals.getOrNull

@Service
@ManagementRequiredTransaction(readOnly = true)
class CompartmentProductCategoryCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<CompartmentProductCategory, Long>,
): ReadonlyCompartmentProductCategoryRepository, CacheRepository<CompartmentProductCategory, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyCompartmentProductCategoryRepository: ReadonlyCompartmentProductCategoryRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<CompartmentProductCategory>(),
                { it.compartmentID },
                readonlyCompartmentProductCategoryRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }
    )

    override fun findByCompartment(compartment: Compartment): CompartmentProductCategory? {
        return findById(compartment.id!!).getOrNull()
    }
}