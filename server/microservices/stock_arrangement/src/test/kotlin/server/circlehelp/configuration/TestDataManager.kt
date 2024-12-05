package server.circlehelp.configuration

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.services.TransactionService
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeoutException
import kotlin.jvm.optionals.getOrNull

@Service
@Scope(SCOPE_PROTOTYPE)
class TestDataManager(
    private val arrivedPackageRepository: ArrivedPackageRepository,
    private val packageProductRepository: PackageProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val entityManager: EntityManager,
    private val transactionService: TransactionService,
    private val applicationContext: ApplicationContext
) {


    private lateinit var _product: Product
    var product: Product
        get() = _product
        set(value) {
            _product = value
        }

    private lateinit var _arrivedPackage: ArrivedPackage
    var arrivedPackage: ArrivedPackage
        get() = _arrivedPackage
        set(value) {
            _arrivedPackage = value
        }

    private lateinit var _packageProduct: PackageProduct
    var packageProduct: PackageProduct
        get() = _packageProduct
        set(value) {
            _packageProduct = value
        }

    private lateinit var _inventoryStock: InventoryStock
    var inventoryStock: InventoryStock
        get() = _inventoryStock
        set(value) {
            _inventoryStock = value
        }

    private lateinit var _productOnCompartment: ProductOnCompartment
    var productOnCompartment: ProductOnCompartment
        get() = _productOnCompartment
        set(value) {
            _productOnCompartment = value
        }

    val proxy by lazy {
        applicationContext.getBean<TestDataManager>()
    }

    fun fetchProduct() = productRepository.findById(product.sku).getOrNull()
    fun fetchArrivedPackage() = arrivedPackageRepository.findById(arrivedPackage.id!!).getOrNull()
    fun fetchPackageProduct() = packageProductRepository.findById(packageProduct.id!!).getOrNull()
    fun fetchInventoryStock() = inventoryRepository.findById(inventoryStock.packageProductID!!).getOrNull()

    data class TestDataItems(
        var inventoryStock: InventoryStock?,
        var packageProduct: PackageProduct?,
        var arrivedPackage: ArrivedPackage?,
        var product: Product?,
    )


    fun getOriginals() = TestDataItems(inventoryStock, packageProduct, arrivedPackage, product)
    fun fetch() = TestDataItems(fetchInventoryStock(), fetchPackageProduct(), fetchArrivedPackage(), fetchProduct())

    private var _initialized: Boolean = false
    var initialized: Boolean
        get() = _initialized
        set(value) {
            _initialized = value
        }

    private var _takeDownValidated = false
    var takeDownValidated: Boolean
        get() = _takeDownValidated
        set(value) {
            _takeDownValidated = value
        }

    fun setInventoryStock(action: InventoryStock.() -> Unit) : InventoryStock {
        fetchInventoryStock()!!.apply(action)
        return inventoryRepository.save(inventoryStock)
    }

    @RepeatableReadTransaction
    fun addStock(
        compartment: Compartment,
        sku: String = "TS9999",
        arrivedDateTime: LocalDateTime = LocalDateTime.of(2024, 4, 1, 1, 1),
        expirationDate: LocalDate = LocalDate.of(2025, 4, 1),
        quantity: Int = 200,
        status: Int = 1
    ) {
        if (initialized) throw UnsupportedOperationException("addStock")

        product = Product(sku, "Test Product: $sku", BigDecimal(99))
        product = productRepository.save(product)

        proxy.product = product

        arrivedPackage = ArrivedPackage("Gehirn", arrivedDateTime)
        arrivedPackage = arrivedPackageRepository.save(arrivedPackage)

        proxy.arrivedPackage = arrivedPackage

        packageProduct = PackageProduct(arrivedPackage, product, quantity, BigDecimal(8), expirationDate)
        packageProduct = packageProductRepository.save(packageProduct)

        proxy.packageProduct = packageProduct

        inventoryStock = InventoryStock(packageProduct, quantity - 1)
        inventoryStock = inventoryRepository.save(inventoryStock)

        proxy.inventoryStock = inventoryStock

        productOnCompartment = productOnCompartmentRepository.save(
            ProductOnCompartment(compartment, packageProduct, status)
        )

        //proxy.productOnCompartment = productOnCompartment

        initialized = true
        //proxy.initialized = true

        if (takeDownValidated.not()) {

            takeDown()
            takeDownValidated = true
            //proxy.takeDownValidated = true

            addStock(compartment, sku, arrivedDateTime, expirationDate, quantity, status)

        }

        initialized = true
    }

    @RepeatableReadTransaction
    fun takeDown() {
        if (initialized.not()) throw UnsupportedOperationException("takeDown")

        productOnCompartmentRepository.delete(productOnCompartment)
        inventoryRepository.delete(inventoryStock)
        packageProductRepository.delete(packageProduct)
        arrivedPackageRepository.delete(arrivedPackage)
        productRepository.delete(product)

        var times = 0
        while (productOnCompartmentRepository.findById(productOnCompartment.compartmentID!!).isPresent) {
            Thread.sleep(100)
            times++
            if (times >= 20)
                throw TimeoutException("$times")
        }

        initialized = false
    }
}