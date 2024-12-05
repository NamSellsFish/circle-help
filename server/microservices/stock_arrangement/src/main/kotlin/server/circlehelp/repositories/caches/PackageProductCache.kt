package server.circlehelp.repositories.caches

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.cache.interceptor.SimpleKeyGenerator
import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository
import server.circlehelp.services.TableWatcherBuilderService
import server.circlehelp.services.TransactionService

@Service
@ManagementRequiredTransaction(readOnly = true)
class PackageProductCache protected constructor(
    private val cacheRepository: CacheRepositoryImpl<PackageProduct, Long>,
    private val readonlyPackageProductRepository: ReadonlyPackageProductRepository,
    private val cacheManager: CacheManager,
): ReadonlyPackageProductRepository, CacheRepository<PackageProduct, Long> by cacheRepository {

    @Autowired
    constructor(
        readonlyPackageProductRepository: ReadonlyPackageProductRepository,
        tableWatcherBuilderService: TableWatcherBuilderService,
        cacheManager: CacheManager,
        transactionService: TransactionService,
        entityManager: EntityManager
    ) : this(
        transactionService.requiredRollbackOnAny {
        CacheRepositoryImpl(
            TableAudit.toSnakeCase<PackageProduct>(),
            { it.id!! },
            readonlyPackageProductRepository,
            tableWatcherBuilderService,
            cacheManager,
            entityManager
        ) }, readonlyPackageProductRepository, cacheManager
    )

    init {
        cacheRepository.updateEvent = ::updateOthers
    }

    private fun updateOthers() {
        packageNProductCache.invalidate()
        packageCache.invalidate()
        productCache.invalidate()
    }

    val tableName = TableAudit.toSnakeCase<PackageProduct>()

    private val packageNProductCache: Cache
        get() = cacheManager.getCache("${tableName}.packageNProduct")!!

    private val packageCache: Cache
        get() = cacheManager.getCache("${tableName}.package")!!

    private val productCache: Cache
        get() = cacheManager.getCache("${tableName}.product")!!

    override fun findByOrderedPackageAndProduct(
        orderedPackage: ArrivedPackage,
        product: Product
    ): PackageProduct? {
        return packageNProductCache.get(SimpleKeyGenerator.generateKey(orderedPackage, product),
            {readonlyPackageProductRepository.findByOrderedPackageAndProduct(orderedPackage, product)})
    }

    override fun findAllByOrderedPackage(orderedPackage: ArrivedPackage): List<PackageProduct> {
        return packageCache.get(orderedPackage,
            {readonlyPackageProductRepository.findAllByOrderedPackage(orderedPackage)})!!
    }

    override fun findAllByProduct(product: Product): List<PackageProduct> {
        return productCache.get(product,
            {readonlyPackageProductRepository.findAllByProduct(product)})!!
    }

}