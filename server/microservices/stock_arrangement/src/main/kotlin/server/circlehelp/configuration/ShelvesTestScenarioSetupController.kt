package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import server.circlehelp.api.baseURL
import server.circlehelp.api.complement
import server.circlehelp.api.test
import server.circlehelp.api.shelf
import server.circlehelp.repositories.CompartmentProductCategoryRepository
import server.circlehelp.repositories.CompartmentRepository
import server.circlehelp.repositories.EventCompartmentRepository
import server.circlehelp.repositories.FrontCompartmentRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductCategorizationRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.RowRepository
import server.circlehelp.repositories.readonly.ReadonlyArrivedPackageRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import server.circlehelp.services.Blocs
import server.circlehelp.services.Logic
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.ShelfAtomicOpsService


const val slowSell = "/slowSell"
const val expiring = "/expiring"

@Controller
@RequestMapping("$baseURL$shelf$test")
class ShelvesTestScenarioSetupController(
    private val productRepository: ProductRepository,
    private val productOnCompartmentRepository: ProductOnCompartmentRepository,
    private val inventoryRepository: InventoryRepository,
    private val rowRepository: RowRepository,
    private val compartmentRepository: CompartmentRepository,
    private val packageProductRepository: PackageProductRepository,
    private val frontCompartmentRepository: FrontCompartmentRepository,
    private val eventCompartmentRepository: EventCompartmentRepository,
    private val compartmentProductCategoryRepository: CompartmentProductCategoryRepository,
    private val productCategorizationRepository: ProductCategorizationRepository,

    private val readonlyArrivedPackageRepository: ReadonlyArrivedPackageRepository,
    private val readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val responseBodyWriter: ResponseBodyWriter,
    private val logic: Logic,
    private val shelfAtomicOpsService: ShelfAtomicOpsService,
    private val blocs: Blocs
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()

    @PostMapping(slowSell)
    @ResponseStatus(HttpStatus.OK)
    fun slowSellEndPoint(@RequestParam count: Int) {
        if (count <= 0) return

        var counter = count

        val emptyCompartments = compartmentRepository.findAll()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() }
            .toList()

        if (emptyCompartments.any().complement()) return

        val compartmentIterator = emptyCompartments.shuffled().iterator()

        for (order in readonlyArrivedPackageRepository.findAllByOrderByDateDescIdDesc()) {

            val inventoryRepositoryIterator = inventoryRepository.findAll().filter {
                logic.isExpiring(it.packageProduct).complement()
            }.iterator()

            while (inventoryRepositoryIterator.hasNext()) {
                val inventoryStock = inventoryRepositoryIterator.next()
                var quantity = inventoryStock.inventoryQuantity
                while (compartmentIterator.hasNext() && quantity > 0) {
                    val compartment = compartmentIterator.next()

                    shelfAtomicOpsService.moveToShelf(
                        inventoryStock, compartment,
                        doLogging = false
                    )
                    quantity--

                    val productOnCompartment =
                        readonlyProductOnCompartmentRepository.findByCompartment(compartment)!!

                    productOnCompartment.status = 3
                    productOnCompartmentRepository.save(productOnCompartment)

                    counter--
                    if (counter <= 0) return
                }

                if (compartmentIterator.hasNext().complement()) return
            }
        }
    }

    @PostMapping(expiring)
    @ResponseStatus(HttpStatus.OK)
    fun expiringEndPoint(@RequestParam count: Int) {
        if (count <= 0) return

        var counter = count

        val emptyCompartments = compartmentRepository.findAll()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() }
            .toList()

        if (emptyCompartments.any().complement()) return

        val compartmentIterator = emptyCompartments.shuffled().iterator()

        for (order in readonlyArrivedPackageRepository.findAllByOrderByDateDescIdDesc()) {

            val inventoryRepositoryIterator = inventoryRepository.findAll().filter {
                logic.isExpiring(it.packageProduct)
            }.iterator()

            while (inventoryRepositoryIterator.hasNext()) {
                val inventoryStock = inventoryRepositoryIterator.next()
                var quantity = inventoryStock.inventoryQuantity
                while (compartmentIterator.hasNext() && quantity > 0) {
                    val compartment = compartmentIterator.next()

                    shelfAtomicOpsService.moveToShelf(
                        inventoryStock, compartment,
                        allowExpiring = true,
                        doLogging = false
                    )
                    quantity--

                    val productOnCompartment =
                        readonlyProductOnCompartmentRepository.findByCompartment(compartment)!!

                    productOnCompartment.status = 2
                    productOnCompartmentRepository.save(productOnCompartment)

                    counter--
                    if (counter <= 0) return
                }

                if (compartmentIterator.hasNext().complement()) return
            }
        }
    }
}