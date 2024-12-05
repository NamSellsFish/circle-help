package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.FrontCompartment
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyFrontCompartmentRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class FrontCompartmentCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<FrontCompartment, Long>
): ReadonlyFrontCompartmentRepository, CacheRepository<FrontCompartment, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyFrontCompartmentRepository: ReadonlyFrontCompartmentRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager,
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<FrontCompartment>(),
                { it.compartmentID },
                readonlyFrontCompartmentRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }
    )

    override fun existsByCompartment(compartment: Compartment): Boolean {
        return existsById(compartment.id!!)
    }

    override fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<FrontCompartment> {
        return findAll(FrontCompartment.numberAscLayerAscSort)
    }
}