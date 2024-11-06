package server.circlehelp.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory
import server.circlehelp.entities.EventCompartment
import server.circlehelp.entities.FrontCompartment
import server.circlehelp.entities.ImageSource
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Layer
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.entities.Shelf
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.CompartmentProductCategoryRepository
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.ImageSourceRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductCategorizationRepository
import server.circlehelp.repositories.ProductCategoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import server.circlehelp.services.Logic
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@Configuration
@Transactional
class SampleDataConfiguration(
    private val arrivedPackageRepository: ArrivedPackageRepository,
    private val packageProductRepository: PackageProductRepository,
    private val productRepository: ProductRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,
    private val shelvesRepository: ShelvesRepository,
    private val rowRepository: RowRepository,
    private val compartmentRepository: CompartmentRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val productCategorizationRepository: ProductCategorizationRepository,
    private val imageSourceRepository: ImageSourceRepository,
    private val compartmentProductCategoryRepository: CompartmentProductCategoryRepository,

    private val logic: Logic,
) {
    init {
        run {
            if (productRepository.count() > 0) return@run

            setupBaseTables()
            setupSpecialCompartments()
            setupProductArrangement()
            eventProduct()
            setupCompartmentProductCategory()
        }

        //addStock()
    }

    /**
     * Seting up sample products and comparment layout.
     * @author Khoa Anh Pham
     */
    private fun setupBaseTables() {
        run {

            val food = ProductCategory("FD", "Food")
            val drink = ProductCategory("DK", "Drink")
            val waterProof = ProductCategory("WP", "Water-proof")

            productCategoryRepository.saveAll(listOf(food, drink, waterProof))

            var encodedYear = 24.toString(36).uppercase(Locale.getDefault())

            if (encodedYear.length == 1)
                encodedYear = "0$encodedYear"

            val ration = Product("${food.id}00${encodedYear}","Ration", BigDecimal(20))
            val waterBottle = Product("${drink.id}00${encodedYear}", "Water Bottle", BigDecimal(10))
            val rainCoat = Product("${waterProof.id}00${encodedYear}", "Rain Coat", BigDecimal(100))

            productRepository.saveAll(listOf(ration, waterBottle, rainCoat))

            productCategorizationRepository.save(ProductCategorization(ration, food))
            productCategorizationRepository.save(ProductCategorization(waterBottle, drink))
            productCategorizationRepository.save(ProductCategorization(rainCoat, waterProof))

            val rationImg = ImageSource(
                "https://placehold.co/400?text=${ration.name.replace(' ', '+')}", ration)
            val waterBottleImg = ImageSource(
                "https://placehold.co/400?text=${waterBottle.name.replace(' ', '+')}", waterBottle)
            val rainCoatImg = ImageSource(
                "https://placehold.co/400?text=${rainCoat.name.replace(' ', '+')}", rainCoat)

            imageSourceRepository.saveAll(listOf(rationImg, waterBottleImg, rainCoatImg))
        }

        var compartmentNum = 0

        run {
            val compartmentCount = 5
            for (i in 1..11) {
                val shelf = Shelf(i)
                shelvesRepository.save(shelf)
                for (j in 1..3) {
                    val row = Layer(shelf, j)
                    rowRepository.save(row)
                    for (k in 1..8) {

                        val compartment = Compartment(row, k, compartmentNum)
                        compartmentRepository.save(compartment)

                        compartmentNum++

                        if ((i in 1..6 || i == 10) && k == 6) break
                        if (i in 7..9 && k == 3) break
                        if (i == 11 && k == 8) break
                    }
                }
            }
        }
    }

    private fun setupSpecialCompartments() {

        val front = { r: Int, l: Int, c: Int ->
            val position = CompartmentPosition(r, l, c)
            val compartment = logic.getCompartment(position).first!!
            val frontCompartment = FrontCompartment(compartment)
            frontCompartmentRepository.save(frontCompartment)
        }

        for (r in 6..11) {
            val shelf = shelvesRepository.findByNumber(r)
            for (row in rowRepository.findAll().filter { it.shelf == shelf })
                for (compartment in compartmentRepository.findAll()
                    .filter { it.layer == row })
                    eventCompartmentRepository.save(EventCompartment(compartment))
        }
        val event = { r: Int, l: Int, c: Int ->
            val position = CompartmentPosition(r, l, c)
            val compartment = logic.getCompartment(position).first!!
            val eventCompartment = EventCompartment(compartment)
            eventCompartmentRepository.save(eventCompartment)
        }
    }

    private fun setupCompartmentProductCategory() {
        for (r in 1..5) {
            val shelf = shelvesRepository.findByNumber(r)
            for (row in rowRepository.findAll().filter { it.shelf == shelf })
                for (compartment in compartmentRepository.findAll()
                    .filter { it.layer == row }) {

                    val categoryID = when(r) {
                        2, 4 -> "DK"
                        3 -> "WP"
                        else -> "FD"
                    }

                    val category = productCategoryRepository.findById(categoryID).get()

                    compartmentProductCategoryRepository.save(CompartmentProductCategory(compartment, category))
                }

        }
    }

    /**
     * Set up an incoming package and added to the inventory.
     * @author Khoa Anh Pham
     */
    private fun setupProductArrangement() {

        val arrivedPackage = ArrivedPackage(LocalDateTime.of(2024, 1, 1, 1, 1), "Gehirn")
        val expirationDate = LocalDate.of(2025, 1, 1)

        arrivedPackageRepository.save(arrivedPackage)

        val pps = { i: PackageProduct -> packageProductRepository.save(i)}

        val products = productRepository.findAll()

        val packageProduct1 = PackageProduct(arrivedPackage, products[0], 10, BigDecimal(15), expirationDate)
        val packageProduct2 = PackageProduct(arrivedPackage, products[1], 15, BigDecimal(8), expirationDate)

        packageProductRepository.save(packageProduct1)
        packageProductRepository.save(packageProduct2)

        val invs = { i: InventoryStock -> inventoryRepository.save(i)}

        invs(InventoryStock(packageProduct1, 10))
        invs(InventoryStock(packageProduct2, 14))

        val compartment =
            logic.getCompartment(CompartmentPosition(1, 2, 3)).first!!
        val compartment2 =
            logic.getCompartment(CompartmentPosition(2, 1, 1)).first!!

        val packageProducts = packageProductRepository.findAll()

        productOnCompartmentRepository.save(ProductOnCompartment(compartment, packageProducts[0], 3))
        productOnCompartmentRepository.save(ProductOnCompartment(compartment2, packageProducts[1]))
    }

    private fun eventProduct() {
        val arrivedPackage = ArrivedPackage(LocalDateTime.of(2024, 4, 1, 1, 1), "EventCompany")
        val expirationDate = null
        val products = productRepository.findAll()
        val packageProduct3 = PackageProduct(arrivedPackage, products[2], 20, BigDecimal(9), expirationDate)

        arrivedPackageRepository.save(arrivedPackage)
        packageProductRepository.save(packageProduct3)
        inventoryRepository.save(InventoryStock(packageProduct3, 20))
    }

    private fun addStock() {
        val arrivedPackage = ArrivedPackage(LocalDateTime.of(2024, 4, 1, 1, 1), "Gehirn")
        val expirationDate = LocalDate.of(2025, 4, 1)

        arrivedPackageRepository.save(arrivedPackage)

        val pps = { i: PackageProduct -> packageProductRepository.save(i)}

        val ration = productRepository.findById("DK000O").get()
        val waterBottle = productRepository.findById("FD000O").get()
        val rainCoat = productRepository.findById("WP000O").get()

        val quantity = 200

        val packageProduct1 = PackageProduct(arrivedPackage, ration, quantity, BigDecimal(15), expirationDate)
        val packageProduct2 = PackageProduct(arrivedPackage, waterBottle, quantity, BigDecimal(8), expirationDate)
        val packageProduct3 = PackageProduct(arrivedPackage, rainCoat, quantity, BigDecimal(80), null)

        packageProductRepository.save(packageProduct1)
        packageProductRepository.save(packageProduct2)
        packageProductRepository.save(packageProduct3)

        val invs = { i: InventoryStock -> inventoryRepository.save(i)}

        invs(InventoryStock(packageProduct1, quantity))
        invs(InventoryStock(packageProduct2, quantity))
        invs(InventoryStock(packageProduct3, quantity))
    }

}