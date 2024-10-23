package server.circlehelp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.EventCompartment
import server.circlehelp.entities.FrontCompartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Layer
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.entities.Shelf
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
import server.circlehelp.utilities.Logic
import java.time.LocalDate
import java.time.LocalDateTime

@Configuration
class SampleDataConfiguration(
    @Autowired private val arrivedPackageRepository: ArrivedPackageRepository,
    @Autowired private val packageProductRepository: PackageProductRepository,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    @Autowired private val inventoryRepository: InventoryRepository,
    @Autowired private val shelvesRepository: ShelvesRepository,
    @Autowired private val rowRepository: RowRepository,
    @Autowired private val compartmentRepository: CompartmentRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,
    @Autowired private val logic: Logic
) {
    init {
        //setupBaseTables()
        //setupSpecialCompartments()
        //setupProductArrangement()
        eventProduct()
    }

    /**
     * Seting up sample products and comparment layout.
     * @author Khoa Anh Pham
     */
    private fun setupBaseTables() {
        run {
            val f = { i: Product -> productRepository.save(i) }

            f(Product("Ration", 20.0))
            f(Product("Water Bottle", 10.0))
            f(Product("Rain Coat", 100.0))
        }

        run {
            val compartmentCount = 5
            for (i in 1..10) {
                val shelf = Shelf(i)
                shelvesRepository.save(shelf)
                for (j in 1..3) {
                    val row = Layer(shelf, j, compartmentCount)
                    rowRepository.save(row)
                    for (k in 1..compartmentCount) {
                        val compartment = Compartment(row, k)
                        compartmentRepository.save(compartment)
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

        for (r in 1..5)
            for (l in 3..3)
                for (c in 1..5)
                    front(r, l, c)

        val event = { r: Int, l: Int, c: Int ->
            val position = CompartmentPosition(r, l, c)
            val compartment = logic.getCompartment(position).first!!
            val eventCompartment = EventCompartment(compartment)
            eventCompartmentRepository.save(eventCompartment)
        }

        for (r in 1..2)
            for (l in 1..3)
                for (c in 4..5)
                    event(r, l, c)
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

        val packageProduct1 = PackageProduct(arrivedPackage, products[0], 10, 15.0, expirationDate)
        val packageProduct2 = PackageProduct(arrivedPackage, products[1], 15, 8.0, expirationDate)

        pps(packageProduct1)
        pps(packageProduct2)

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
        val packageProduct3 = PackageProduct(arrivedPackage, products[2], 20, 9.0, expirationDate)

        arrivedPackageRepository.save(arrivedPackage)
        packageProductRepository.save(packageProduct3)
        inventoryRepository.save(InventoryStock(packageProduct3, 20))
    }

}