package server.circlehelp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Layer
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.entities.Shelf
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.ShelvesRepository
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
    @Autowired private val compartmentRepository: CompartmentRepository
) {
    init {
        //setupBaseTables()
        //setupProductArrangement()
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

        pps(PackageProduct(arrivedPackage, products[0], 10, 15.0, expirationDate))
        pps(PackageProduct(arrivedPackage, products[1], 15, 8.0, expirationDate))

        val invs = { i: InventoryStock -> inventoryRepository.save(i)}

        invs(InventoryStock(products[0], arrivedPackage, 10))
        invs(InventoryStock(products[0], arrivedPackage, 14))

        val shelf = shelvesRepository.findByNumber(1)!!
        val row = rowRepository.findByShelfAndNumber(shelf, 2)!!
        val compartment = compartmentRepository.findByLayerAndNumber(row, 3)!!

        productOnCompartmentRepository.save(ProductOnCompartment(compartment, products[1], arrivedPackage))
    }

}