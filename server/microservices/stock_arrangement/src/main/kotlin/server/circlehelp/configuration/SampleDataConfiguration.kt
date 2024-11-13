package server.circlehelp.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.Transactional
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory
import server.circlehelp.entities.Event
import server.circlehelp.entities.EventCompartment
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.FrontCompartment
import server.circlehelp.entities.ImageSource
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Layer
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.CompartmentProductCategoryRepository
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.EventProductRepository
import server.circlehelp.repositories.EventRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.ImageSourceRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductCategorizationRepository
import server.circlehelp.repositories.ProductCategoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.readonly.ReadonlyEventProductRepository
import server.circlehelp.repositories.readonly.ReadonlyEventRepository
import server.circlehelp.services.Blocs
import server.circlehelp.services.Logic
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import java.util.stream.Collectors.groupingBy

@Configuration
@Transactional
class SampleDataConfiguration(
    private val arrivedPackageRepository: ArrivedPackageRepository,
    private val packageProductRepository: PackageProductRepository,
    private val productRepository: ProductRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,
    private val rowRepository: RowRepository,
    private val blocs: Blocs,
    private val compartmentRepository: CompartmentRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val productCategorizationRepository: ProductCategorizationRepository,
    private val imageSourceRepository: ImageSourceRepository,
    private val compartmentProductCategoryRepository: CompartmentProductCategoryRepository,
    private val eventRepository: EventRepository,
    private val eventProductRepository: EventProductRepository,

    private val readonlyEventRepository: ReadonlyEventRepository,
    private val readonlyEventProductRepository: ReadonlyEventProductRepository,

    private val logic: Logic,
) {
    init {
        run {
            if (productRepository.count() > 0) return@run

            setupBaseTables()
            setupSpecialCompartments()
            setupProductArrangement()
            expiredPackage()

            eventProduct()
            setupCompartmentProductCategory()
            addStock()
        }

        //setupSpecialCompartments()
        //addStock()
        //setupCompartmentProductCategory()
        //shelfRemovalUpdatePatch()
        //setupSpecialCompartments()
        //compartmentNumberingByShelf()
        //lunchablesProduct()
        //lunchablesDelivery()
        //eventProduct()
    }

    @Deprecated("Use 'compartmentNumberingByShelf'")
    private fun setCompartmentNoFromUserPerspective() {

        val compartmentsByLayer =
            compartmentRepository
                .findAll()
                .stream()
                .collect(groupingBy { it.layer.number })

        for ((_, compartments) in compartmentsByLayer) {
            for ((compartmentNum, compartment) in compartments.withIndex()) {
                val no = compartment.number
                val shelfNo = blocs.compartmentIndexShelfMap[no]!!
                compartment.compartmentNoFromUserPerspective =
                    "${shelfNo+1}-${blocs.shelfCompartmentsMap[shelfNo].indexOf(no) + 1}"
                compartmentRepository.save(compartment)
            }
        }
    }

    /*
    private fun shelfRemovalUpdatePatch() {

        for (j in 1..3) {
            val row = Layer(j)
            rowRepository.save(row)
        }

        val rows = rowRepository.findAll()

        var rowIterator = rows.iterator()
        var sequenceIterator = blocs.sequence.iterator()

        for (compartment in compartmentRepository.findAll()) {

            if (! rowIterator.hasNext())
                rowIterator = rows.iterator()

            if (! sequenceIterator.hasNext())
                sequenceIterator = blocs.sequence.iterator()

            compartment.layer = rowIterator.next()
            compartment.number = sequenceIterator.next()
            compartment.compartmentNoFromUserPerspective = compartment.number + 1

            compartmentRepository.save(compartment)
        }
    }
     */


    private fun compartmentNumberingByShelf() {
        for (compartment in compartmentRepository.findAll()) {
            val no = compartment.number
            val shelfNo = blocs.compartmentIndexShelfMap[no]!!
            compartment.compartmentNoFromUserPerspective =
                "${blocs.shelfNoToCharString(shelfNo)}-${blocs.shelfCompartmentsMap[shelfNo].indexOf(no) + 1}"
            compartmentRepository.save(compartment)
        }
    }

    private fun lunchablesProduct() {

        val foodCategory = productCategoryRepository.findById("FD").get()
        val drinkCategory = productCategoryRepository.findById("DK").get()
        val encodedYear = 24.toString(36).uppercase(Locale.getDefault()).let {
            if (it.length == 1) "0$it" else it
        }

        val lunchableProduct = Product(
            "${foodCategory.id}01$encodedYear",
            "Lunch-ables",
            BigDecimal(50)
        )
        productRepository.save(lunchableProduct)

        productCategorizationRepository.save(ProductCategorization(lunchableProduct, foodCategory))
        productCategorizationRepository.save(ProductCategorization(lunchableProduct, drinkCategory))
    }

    private fun lunchablesDelivery() {

        val arrivalDate = LocalDateTime.of(2024, 6, 6, 6, 0)

        val arrivedPackage = ArrivedPackage(
            arrivalDate,
            "Reichtümer"
        )
        arrivedPackageRepository.save(arrivedPackage)

        val foodCategory = productCategoryRepository.findById("FD").get()
        val drinkCategory = productCategoryRepository.findById("DK").get()
        val encodedYear = 24.toString(36).uppercase(Locale.getDefault()).let {
            if (it.length == 1) "0$it" else it
        }

        val product = productRepository.findById("${foodCategory.id}01${encodedYear}").get()

        val packageProduct = PackageProduct(
            arrivedPackage,
            product,
            100,
            BigDecimal(40),
            arrivalDate.plusMonths(12).toLocalDate())
        packageProductRepository.save(packageProduct)

        inventoryRepository.save(InventoryStock(
            packageProduct,
            packageProduct.importedQuantity
        ))
    }

    /**
     * Setting up sample products and compartment layout.
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

        run {
            for (j in 1..3) {
                val row = Layer(j)
                rowRepository.save(row)
                for (k in blocs.sequence) {
                    val compartment = Compartment(row, k, (k + 1).toString())
                    compartmentRepository.save(compartment)
                }
            }
        }
    }

    private fun setupSpecialCompartments() {

        for (row in rowRepository.findAll())
            for (compartment in compartmentRepository.findAll()
                .filter { it.layer.id == row.id })
                if (blocs.compartmentIndexBlocMap[compartment.number] in 3..4)
                    frontCompartmentRepository.save(FrontCompartment(compartment))

        for (row in rowRepository.findAll())
            for (compartment in compartmentRepository.findAll()
                .filter { it.layer.id == row.id })
                if (blocs.compartmentIndexBlocMap[compartment.number] in 1..2)
                    eventCompartmentRepository.save(EventCompartment(compartment))

    }

    private fun setupCompartmentProductCategory() {

        for (row in rowRepository.findAll())
            for (compartment in compartmentRepository.findAll()
                .filter { it.layer == row }) {

                val categoryID = when (blocs.compartmentIndexShelfMap[compartment.number]) {
                    1, 4 -> "DK"
                    2 -> "WP"
                    0, 3 -> "FD"
                    else -> null
                }

                if (categoryID != null) {

                    val category = productCategoryRepository.findById(categoryID).get()

                    compartmentProductCategoryRepository.save(
                        CompartmentProductCategory(
                            compartment,
                            category
                        )
                    )
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
            logic.getCompartment(CompartmentPosition(2, 3)).first!!
        val compartment2 =
            logic.getCompartment(CompartmentPosition(1, 1)).first!!

        val packageProducts = packageProductRepository.findAll()

        productOnCompartmentRepository.save(ProductOnCompartment(compartment, packageProducts[0], 3))
        productOnCompartmentRepository.save(ProductOnCompartment(compartment2, packageProducts[1]))
    }

    private fun expiredPackage() {
        val arrivedPackage = ArrivedPackage(LocalDateTime.of(2023, 1, 1, 1, 1), "Gehirn")
        val expirationDate = LocalDate.of(2024, 1, 1)

        arrivedPackageRepository.save(arrivedPackage)

        val pps = { i: PackageProduct -> packageProductRepository.save(i)}

        val products = productRepository.findAll()

        val packageProduct1 = PackageProduct(arrivedPackage, products[0], 210, BigDecimal(15), expirationDate)
        val packageProduct2 = PackageProduct(arrivedPackage, products[1], 215, BigDecimal(8), expirationDate)

        packageProductRepository.save(packageProduct1)
        packageProductRepository.save(packageProduct2)

        val invs = { i: InventoryStock -> inventoryRepository.save(i)}

        invs(InventoryStock(packageProduct1, 210))
        invs(InventoryStock(packageProduct2, 214))

        val compartment =
            logic.getCompartment(CompartmentPosition(2, 3)).first!!
        val compartment2 =
            logic.getCompartment(CompartmentPosition(2, 2)).first!!

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

        val startDate = LocalDate.of(2024, 3, 25)
        val endDate = LocalDate.of(2025, 1, 26)
        val event = Event("Event Company's Event", startDate, endDate)
        val eventProduct1 = EventProduct(products[2], event)
        val eventProduct2 = EventProduct(products[1], event)

        eventRepository.save(event)
        eventProductRepository.save(eventProduct1)
        eventProductRepository.save(eventProduct2)
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