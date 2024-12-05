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
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService
import kotlin.jvm.optionals.getOrNull

@Service
@ManagementRequiredTransaction(readOnly = true)
class InventoryStockCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<InventoryStock, Long>,
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val cacheManager: CacheManager,
): ReadonlyInventoryRepository, CacheRepository<InventoryStock, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyInventoryRepository: ReadonlyInventoryRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager,
        @Qualifier("inventoryStockDependencies") inventoryStockDependencies: DependencyNode,
    ) : this(
        transactionService.requiredRollbackOnAny {
            CacheRepositoryImpl(
                TableAudit.toSnakeCase<InventoryStock>(),
                { it.packageProductID },
                readonlyInventoryRepository,
                tableWatcherBuilderService,
                cacheManager,
                entityManager,
                inventoryStockDependencies.streamAll().map { TableAudit.toSnakeCase(it.item.simpleName!!) }.toList().let { it.takeLast(it.size - 1) }
            )
        }, readonlyInventoryRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() {
        orderCache.invalidate()
    }

    val tableName = TableAudit.toSnakeCase<InventoryStock>()

    private val orderCache: Cache
        get() = cacheManager.getCache("${tableName}.order")!!

    override fun existsByPackageProduct(packageProduct: PackageProduct): Boolean {
        return existsById(packageProduct.id!!)
    }

    override fun findByPackageProduct(packageProduct: PackageProduct): InventoryStock? {
        return findById(packageProduct.id!!).getOrNull()
    }

    override fun findByPackageProductOrderedPackage(orderedPackage: ArrivedPackage): List<InventoryStock> {
        return orderCache.get(orderedPackage,
            {readonlyInventoryRepository.findByPackageProductOrderedPackage(orderedPackage)})!!
    }

    override fun findAllByOrderByPackageProductExpirationDateDesc(): List<InventoryStock> {
        return findAll(InventoryStock.expirationDateDescSort)
    }

    override fun findIdByPackageProductId(id: Long): Long? {
        return readonlyInventoryRepository.findIdByPackageProductId(id)
    }

    override fun findAllByOrderByPackageProductOrderedPackageDateTimeDescPackageProductOrderedPackageIdDesc(): List<InventoryStock> {
        return findAll(InventoryStock.newestImportSort)
    }
}