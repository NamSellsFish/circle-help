package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.Layer
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyCompartmentRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class CompartmentCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<Compartment, Long>,
    private val readonlyCompartmentRepository: ReadonlyCompartmentRepository,
    private val cacheManager: CacheManager,
): ReadonlyCompartmentRepository, CacheRepository<Compartment, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyCompartmentRepository: ReadonlyCompartmentRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
        CacheRepositoryImpl(
            TableAudit.toSnakeCase<Compartment>(),
            { it.id!! },
            readonlyCompartmentRepository,
            tableWatcherBuilderService,
            cacheManager,
            entityManager
        )}, readonlyCompartmentRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() { coordCache.invalidate() }

    val tableName = TableAudit.toSnakeCase<Compartment>()

    val coordCache: Cache
        get() = cacheManager.getCache("${tableName}.coord")!!

    override fun findByLayerAndNumber(layer: Layer, number: Int): Compartment? {
        return coordCache.get(SimpleKeyGenerator.generateKey(layer, number),
            {readonlyCompartmentRepository.findByLayerAndNumber(layer, number)})
    }

    override fun findAllByOrderByNumberAscLayerNumberAsc(): List<Compartment> {
        return findAll(Compartment.numberAscLayerAscSort)
    }

    override fun findAllByOrderByLayerNumberAscNumberAsc(): List<Compartment> {
        return findAll(Compartment.layerAscNumberAscSort)
    }
}