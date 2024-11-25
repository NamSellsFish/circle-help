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
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.ProductOnCompartment
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
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository
import server.circlehelp.services.Blocs
import server.circlehelp.services.Logic
import server.circlehelp.services.ResponseBodyWriter
import server.circlehelp.services.ShelfAtomicOpsService
import java.util.LinkedList
import kotlin.random.Random


const val slowSell = "/slowSell"
const val expiring = "/expiring"
const val remove = "/remove"

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
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val responseBodyWriter: ResponseBodyWriter,
    private val logic: Logic,
    private val shelfAtomicOpsService: ShelfAtomicOpsService,
    private val blocs: Blocs
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()

    @PostMapping(slowSell)
    @ResponseStatus(HttpStatus.OK)
    fun slowSellEndPoint(@RequestParam count: Int,
                         @RequestParam seed: Int?,
                         @RequestParam(defaultValue = "true") slowSell: Boolean) {
        if (count <= 0) return

        var counter = count

        val random = getRandom(seed, 0)

        val emptyCompartments = compartmentRepository.findAll()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() }
            .toList()

        if (emptyCompartments.any().complement()) return

        val compartmentIterator = emptyCompartments.shuffled(random).take(count).iterator()

        for (order in readonlyArrivedPackageRepository.findAllByOrderByDateTimeDescIdDesc()) {

            continuousArrangement(
                LinkedList(readonlyInventoryRepository.findAll().filter {
                    logic.isExpiring(it.packageProduct).not()
                }) ,
                compartmentIterator
            ) { status = 3 }

            if (compartmentIterator.hasNext().not()) return

            /*
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

             */
        }
    }

    @PostMapping(expiring)
    @ResponseStatus(HttpStatus.OK)
    fun expiringEndPoint(@RequestParam count: Int, @RequestParam seed: Int?) {
        if (count <= 0) return

        var counter = count

        val random = getRandom(seed, 1)

        val emptyCompartments = compartmentRepository.findAll()
            .filter { readonlyProductOnCompartmentRepository.existsByCompartment(it).complement() }
            .toList()

        if (emptyCompartments.any().complement()) return

        val compartmentIterator = emptyCompartments.shuffled(random).take(count).iterator()

        for (order in readonlyArrivedPackageRepository.findAllByOrderByDateTimeDescIdDesc()) {

            continuousArrangement(
                LinkedList(readonlyInventoryRepository.findAll().filter {
                    logic.isExpiring(it.packageProduct)
                }),
                compartmentIterator
            ) { status = 2 }

            if (compartmentIterator.hasNext().not()) return

            /*
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
             */
        }
    }

    @PostMapping(remove)
    @ResponseStatus(HttpStatus.OK)
    fun removeRandomEndPoint(@RequestParam count: Int, @RequestParam seed: Int?) {
        if (count <= 0) return

        val random = getRandom(seed, 0)

        val filledCompartments = readonlyProductOnCompartmentRepository
            .findAll()

        if (filledCompartments.any().complement()) return

        val compartmentIterator = filledCompartments.shuffled(random).iterator()

        (1..count).forEach { _ -> shelfAtomicOpsService.moveToInventory(compartmentIterator.next()) }
    }

    /**
     * Copied from [server.circlehelp.services.ShelfService]
     */
    private fun continuousArrangement(inventoryStocks: LinkedList<InventoryStock>,
                                      compartmentIterator: Iterator<Compartment>,
                                      productOnCompartmentFunc: ProductOnCompartment.() -> Unit = {}): String {

        val stringBuilder = StringBuilder()

        var inventoryStockMutableIterator = inventoryStocks.iterator()
        //val compartmentIterator = compartments.iterator()

        while (inventoryStockMutableIterator.hasNext() && compartmentIterator.hasNext()) {

            val inventoryStock = inventoryStockMutableIterator.next()

            if (readonlyInventoryRepository.existsById(inventoryStock.packageProductID!!).not()) {
                inventoryStockMutableIterator.remove()
                continue
            }

            val compartment = compartmentIterator.next()

            stringBuilder.appendLine(shelfAtomicOpsService.moveToShelf(
                inventoryStock,compartment, productOnCompartmentFunc, allowExpiring = true))

            if (inventoryStockMutableIterator.hasNext().complement()) {
                inventoryStockMutableIterator = inventoryStocks.iterator()
            }
        }

        return stringBuilder.toString()
    }

    private fun getRandom(seed: Int? = null, salt: Int = 0) : Random {
        return seed.let {
            if (it == null)
                Random.Default
            else
                Random(it + salt)
        }
    }
}