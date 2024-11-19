package server.circlehelp.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.configuration.TestDataManager
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository
import server.circlehelp.repositories.readonly.ReadonlyProductOnCompartmentRepository

@RepeatableReadTransaction
@SpringBootTest
@Rollback
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ShelfAtomicOpsServiceTest {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var productOnCompartmentRepository: ProductOnCompartmentRepository
    @Autowired
    lateinit var inventoryRepository: InventoryRepository

    @Autowired
    lateinit var readonlyProductOnCompartmentRepository: ReadonlyProductOnCompartmentRepository
    @Autowired
    lateinit var readonlyInventoryRepository: ReadonlyInventoryRepository
    @Autowired
    lateinit var readonlyPackageProductRepository: ReadonlyPackageProductRepository

    @Autowired
    lateinit var shelfAtomicOpsService: ShelfAtomicOpsService
    @Autowired
    lateinit var logic: Logic

    @Autowired
    lateinit var callerService: CallerService

    @Autowired
    lateinit var testDataManager: TestDataManager

    @Autowired
    lateinit var testDataManager2: TestDataManager

    @BeforeAll
    fun setUp() {

            testDataManager.addStock(logic.getCompartment(CompartmentPosition(2, 1)).first!!)
            testDataManager2.addStock(
                logic.getCompartment(CompartmentPosition(2, 2)).first!!, "TS0000"
            )

    }

    @AfterAll
    fun takeDown() {
        listOf(
            testDataManager,
            testDataManager2,
        ).forEach {
            it.takeDown()
        }
    }

    /*
    Move To Shelf:
    Inventory Stock:
    - quantity == 1
    - quantity > 1 (default)
    ProductOnCompartment:
    - null (default)
    - not null
     */


    @Test
    fun moveToShelf() {

        val compartment =
            logic.getCompartment(CompartmentPosition(1, 1))
                .first!!

        testDataManager.setInventoryStock { inventoryQuantity = 1 }

        var (inventoryStock, packageProduct) = testDataManager.fetch()

        shelfAtomicOpsService.moveToShelf(inventoryStock!!, compartment)

        val productOnCompartment = readonlyProductOnCompartmentRepository
            .findByCompartment(compartment)!!

        assertEquals(productOnCompartment.compartment, compartment)
        assertEquals(productOnCompartment.packageProduct, packageProduct)
        assertEquals(testDataManager.fetchInventoryStock()!!.inventoryQuantity,
            inventoryStock.inventoryQuantity - 1)

        entityManager.flush()
    }

    @Test
    fun moveToShelfQuantityOne() {

        val compartment =
            logic.getCompartment(CompartmentPosition(1, 1))
                .first!!

        var (inventoryStock, packageProduct) = testDataManager.fetch()

        inventoryStock!!.inventoryQuantity = 1

        shelfAtomicOpsService.moveToShelf(inventoryStock, compartment)

        val productOnCompartment = readonlyProductOnCompartmentRepository
            .findByCompartment(compartment)!!

        inventoryStock = testDataManager.fetch().inventoryStock

        assertEquals(productOnCompartment.compartment, compartment)
        assertEquals(productOnCompartment.packageProduct, packageProduct)
        assertNull(inventoryStock)

        entityManager.flush()
    }

    @Test
    fun moveToShelfOccupied() {

        val compartment =
            logic.getCompartment(CompartmentPosition(1, 1))
                .first!!

        var (inventoryStock, packageProduct) = testDataManager.fetch()

        shelfAtomicOpsService.moveToShelf(inventoryStock!!, compartment)

        var productOnCompartment = readonlyProductOnCompartmentRepository
            .findByCompartment(compartment)!!

        assertEquals(productOnCompartment.compartment, compartment)
        assertEquals(productOnCompartment.packageProduct, packageProduct)
        assertEquals(testDataManager.fetchInventoryStock()!!.inventoryQuantity,
            inventoryStock.inventoryQuantity - 1)

        var (inventoryStock2, packageProduct2) = testDataManager2.fetch()

        shelfAtomicOpsService.moveToShelf(inventoryStock2!!, compartment)

        productOnCompartment = readonlyProductOnCompartmentRepository
            .findByCompartment(compartment)!!

        assertEquals(productOnCompartment.packageProduct, packageProduct2)
        assertEquals(testDataManager.fetchInventoryStock()!!.inventoryQuantity,
            inventoryStock.inventoryQuantity)
        assertEquals(testDataManager2.fetchInventoryStock()!!.inventoryQuantity,
            inventoryStock2.inventoryQuantity - 1)

        entityManager.flush()
    }

    @Test
    fun testMoveToShelf() {

        assertEquals(testDataManager.fetchInventoryStock()!!.inventoryQuantity,
            testDataManager.inventoryStock.inventoryQuantity)
        entityManager.flush()
    }


    @Test
    fun swapStockPlacementBothFilled() {

        val compartmentSrc = logic.getCompartment(CompartmentPosition(2, 1)).first!!
        val compartmentDes = logic.getCompartment(CompartmentPosition(2, 2)).first!!

        val productOnCompartmentSrcStart =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc)!!
        val productOnCompartmentDesStart =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentDes)!!

        shelfAtomicOpsService.swapStockPlacement(compartmentSrc, compartmentDes)

        val productOnCompartmentSrcEnd =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc)!!
        val productOnCompartmentDesEnd =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentDes)!!

        val selectors : List<(ProductOnCompartment) -> Any> = listOf(
            { it.packageProduct }, { it.status }
        )

        selectors.forEach {

            assertEquals(
                it(productOnCompartmentSrcStart),
                it(productOnCompartmentDesEnd)
            )

            assertEquals(
                it(productOnCompartmentDesStart),
                it(productOnCompartmentSrcEnd)
            )
        }
    }

    @Test
    fun swapStockPlacementDesEmpty() {

        val compartmentSrc = logic.getCompartment(CompartmentPosition(2, 1)).first!!
        val compartmentDes = logic.getCompartment(CompartmentPosition(2, 3)).first!!

        val productOnCompartmentSrcStart =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc)!!
        assertNull(
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentDes))

        shelfAtomicOpsService.swapStockPlacement(compartmentSrc, compartmentDes)

        assertNull(
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc))
        val productOnCompartmentDesEnd =
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentDes)!!

        val selectors : List<(ProductOnCompartment) -> Any> = listOf(
            { it.packageProduct }, { it.status }
        )

        selectors.forEach {

            assertEquals(
                it(productOnCompartmentSrcStart),
                it(productOnCompartmentDesEnd)
            )
        }
    }

    @Test
    fun moveToInventory() {

        val compartmentSrc = logic.getCompartment(CompartmentPosition(2, 1)).first!!
        val compartmentDes = logic.getCompartment(CompartmentPosition(2, 3)).first!!

        val productOnCompartmentSrcStart=
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc)

        shelfAtomicOpsService.swapStockPlacement(compartmentSrc, compartmentDes)

        assertNull(
            readonlyProductOnCompartmentRepository.findByCompartment(compartmentSrc))


    }

    @Test
    fun removeAll() {
    }
}