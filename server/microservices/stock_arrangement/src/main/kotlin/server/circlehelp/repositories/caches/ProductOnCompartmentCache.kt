package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.delegated_classes.DependencyNode
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService
import kotlin.jvm.optionals.getOrNull

@Service
@ManagementRequiredTransaction(readOnly = true)
class ProductOnCompartmentCache  protected constructor(
    private val cacheRepository: CacheRepositoryImpl<ProductOnCompartment, Long>,
    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
    private val cacheManager: CacheManager,
): ReadonlyProductOnCompartmentRepository, CacheRepository<ProductOnCompartment, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager,
        @Qualifier("productOnCompartmentDependencies") productOnCompartmentDependencies: DependencyNode
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<ProductOnCompartment>(),
                { it.compartmentID },
                readonlyProductOnCompartmentRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager,
                productOnCompartmentDependencies.streamAll().map { TableAudit.toSnakeCase(it.item.simpleName!!) }.toList().let { it.takeLast(it.size - 1) }
            )
        }, readonlyProductOnCompartmentRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() {
        statusCache.invalidate()
    }

    val tableName = TableAudit.toSnakeCase<ProductOnCompartment>()

    private val statusCache: Cache
        get() = cacheManager.getCache("${tableName}.status")!!


    override fun existsByCompartment(compartment: Compartment): Boolean {
        return existsById(compartment.id!!)
    }

    override fun findByCompartment(compartment: Compartment): ProductOnCompartment? {
        return findById(compartment.id!!).getOrNull()
    }

    override fun findAllByStatus(status: Int): List<ProductOnCompartment> {
        return statusCache.get(status)
        {readonlyProductOnCompartmentRepository.findAllByStatus(status)}!!
    }

    override fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<ProductOnCompartment> {
        return findAll(ProductOnCompartment.numberAscLayerAscSort)
    }

    override fun findAllByOrderByCompartmentLayerNumberAscCompartmentNumberAsc(): List<ProductOnCompartment> {
        return findAll(ProductOnCompartment.layerAscNumberAscSort)
    }

    override fun findAllId(): List<Long> {
        return readonlyProductOnCompartmentRepository.findAllId()
    }

    override fun findPackageProductIdById(id: Long): Long? {
        return readonlyProductOnCompartmentRepository.findPackageProductIdById(id)
    }


}