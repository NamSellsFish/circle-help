package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.EventCompartment
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyEventCompartmentRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class EventCompartmentCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<EventCompartment, Long>
): ReadonlyEventCompartmentRepository, CacheRepository<EventCompartment, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyEventCompartmentRepository: ReadonlyEventCompartmentRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<EventCompartment>(),
                { it.compartmentID },
                readonlyEventCompartmentRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager
            )
        }
    )

    override fun existsByCompartment(compartment: Compartment): Boolean {
        return cacheRepository.existsById(compartment.id!!)
    }

    override fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<EventCompartment> {
        return findAll(EventCompartment.numberAscLayerAscSort)
    }
}