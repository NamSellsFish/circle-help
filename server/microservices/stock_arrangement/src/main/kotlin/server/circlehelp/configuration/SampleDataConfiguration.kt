package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.EmployeeManagementController
import server.circlehelp.api.request.AttendanceRequest
import server.circlehelp.api.request.AttendanceRequestType
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceResponse
import server.circlehelp.api.response.AttendanceType
import server.circlehelp.api.response.CompartmentPosition
import server.circlehelp.auth.AccountRepository
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Attendance
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory
import server.circlehelp.entities.Event
import server.circlehelp.entities.EventCompartment
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.FrontCompartment
import server.circlehelp.entities.ImageSource
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Layer
import server.circlehelp.entities.Location
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.entities.TableAudit
import server.circlehelp.entities.WorkShift
import server.circlehelp.entities.WorkplaceLocation
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.AttendanceRepository
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
import server.circlehelp.repositories.TableAuditRepository
import server.circlehelp.repositories.WorkShiftRepository
import server.circlehelp.repositories.WorkplaceLocationRepository
import server.circlehelp.repositories.readonly.ReadonlyAttendanceRepository
import server.circlehelp.repositories.readonly.ReadonlyEventProductRepository
import server.circlehelp.repositories.readonly.ReadonlyEventRepository
import server.circlehelp.repositories.readonly.ReadonlyWorkShiftRepository
import server.circlehelp.services.AttendanceArbiterManager
import server.circlehelp.services.Blocs
import server.circlehelp.services.TransactionService
import server.circlehelp.services.Logic
import server.circlehelp.services.Logic.Companion.d
import server.circlehelp.value_classes.UrlValue
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.util.Locale
import java.util.stream.Collectors.groupingBy
import java.util.stream.Stream
import kotlin.random.Random

@Configuration
@RepeatableReadTransaction
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
    private val tableAuditRepository: TableAuditRepository,
    private val workShiftRepository: WorkShiftRepository,
    private val workplaceLocationRepository: WorkplaceLocationRepository,
    private val attendanceRepository: AttendanceRepository,

    private val readonlyEventRepository: ReadonlyEventRepository,
    private val readonlyEventProductRepository: ReadonlyEventProductRepository,
    private val readonlyWorkShiftRepository: ReadonlyWorkShiftRepository,

    private val logic: Logic,
    private val entityManager: EntityManager,
    private val transactionService: TransactionService,
    private val accountRepository: AccountRepository,
    private val readonlyAttendanceRepository: ReadonlyAttendanceRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder,
    private val objectMapper: ObjectMapper,
    attendanceArbiterManager: AttendanceArbiterManager,
) {

    private val logger = LoggerFactory.getLogger(SampleDataConfiguration::class.java)
    private val attendanceArbiter = attendanceArbiterManager.topAttendanceArbiter

    private data class StockInfo(
        val arrivedDateTime: LocalDateTime = LocalDateTime.of(2024, 4, 1, 1, 1),
        val expirationDate: LocalDate = LocalDate.of(2025, 4, 1),
        val quantity: Int = 200
    )

    init {

        transactionService.requiredRollbackOnAny {

            logger.info(objectMapper.writeValueAsString(LocalDateTime.now()))

            logger.info(objectMapper.writeValueAsString(OrderApprovalRequest.sample))
            logger.info(objectMapper.writeValueAsString(
                AttendanceRequest(
                    AttendanceRequestType.PunchIn,
                    LocalDate.now(),
                    LocalTime.now(),
                    LocalTime.now(),
                    "https://placehold.co/400?text=Water+Bottle",
                    BigDecimal( 10.8464752),
                    BigDecimal(106.7784394)
                )
            ))
            logger.info(objectMapper.writeValueAsString(
                AttendanceResponse(
                    AttendanceType.FullAttendance,
                    LocalDate.now(),
                    LocalTime.now(),
                    LocalTime.now(),
                    "https://placehold.co/400?text=Package"
                )
            ))

            run {
                addTableAuditEntries()
                addWorkShiftEntries()
                addWorkplaceLocation()
                addAttendanceData()

                if (productRepository.count() > 0) return@run

                setupBaseTables()
                setupSpecialCompartments()
                setupProductArrangement()
                expiredPackage()
                compartmentNumberingByShelf()

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

            // Add several package products.
            run {
                return@run
                val stockInfos = listOf(
                    StockInfo(
                        arrivedDateTime = LocalDateTime.of(2023, 4, 1, 12, 0),
                        expirationDate = LocalDate.of(2024, 12, 12),
                        quantity = 20
                    ),
                    StockInfo(
                        arrivedDateTime = LocalDateTime.of(2024, 9, 9, 9, 0),
                        expirationDate = LocalDate.of(2025, 4, 1),
                        quantity = 10
                    ),
                    StockInfo(
                        arrivedDateTime = LocalDateTime.of(2024, 10, 1, 10, 0),
                        expirationDate = LocalDate.of(2025, 10, 1),
                        quantity = 30
                    )
                )

                stockInfos.forEach { addStock(it.arrivedDateTime, it.expirationDate, it.quantity) }
            }


            val arrivedDateTime = LocalDateTime.of(2024, 9, 9, 9, 0)
            val expirationDate = LocalDate.of(2025, 4, 1)
            val quantity = 33

            /*
            addEventAndNonEventStock(arrivedDateTime, expirationDate, quantity)
             */

            /*
            (1..9L).map {
                lunchablesDelivery(arrivedDateTime.plusWeeks(it), 3)
            }

             */
        }
    }

    private fun addAttendanceData() {
        if (attendanceRepository.count().toInt() == 0) {
            val today = LocalDate.now()
            val workDays = getWeekdaysInMonth(today.year, today.month.value - 1)
                .stream().let {
                    Stream.concat(it,
                        getWeekdaysInMonth(today.year, today.month.value)
                            .stream()
                            .filter { it.dayOfMonth < today.dayOfMonth }
                        )
                }

            val user = accountRepository.findById("employee@email.com").get()
            val workShift = workShiftRepository.findById("employee@email.com").get()

            workDays.forEach {
                listOf(AttendanceRequestType.PunchIn, AttendanceRequestType.PunchOut).map { type ->

                    if (type == AttendanceRequestType.PunchOut && Random.nextInt().mod(3) == 0) return@map Unit

                    val attendance = readonlyAttendanceRepository.findByUserAndDate(user, it)

                    val attendanceRequest = AttendanceRequest(
                        type,
                        it,
                        LocalTime.of(8, 0).plusMinutes(Random.nextLong().mod(120L) - 60),
                        LocalTime.of(17, 30).plusMinutes(Random.nextLong().mod(120L) - 60),
                        "http://res.cloudinary.com/dkirx8mro/image/upload/v1733332265/samples/circle-help/file_vfhuha.jpg",
                        0.d, 0.d
                    )
                    val result = attendanceArbiter.decide(
                        ArbitratedAttendance(
                            attendanceRequest.currDate,
                            if (attendanceRequest.type == AttendanceRequestType.PunchIn)
                                attendanceRequest.punchInTime
                            else
                                null,
                            if (attendanceRequest.type == AttendanceRequestType.PunchOut)
                                attendanceRequest.punchOutTime
                            else
                                null,
                            UrlValue(attendanceRequest.imageUrl)
                        ), workShift!!
                    )

                    if (attendanceRequest.type == AttendanceRequestType.PunchIn)
                        attendanceRepository.save(
                            Attendance(
                                user,
                                result.currDate,
                                result.punchInTime!!,
                                null,
                                UrlValue(result.imageUrl),
                                result.type
                            )
                        )
                    else
                        attendanceRepository.save(attendance!!.apply {
                            punchOutTime = attendanceRequest.punchOutTime!!
                            this.type = result.type
                        })

                    Unit
                }
            }
        }
    }

    fun getWeekdaysInMonth(year: Int, month: Int): List<LocalDate> {
        val yearMonth = YearMonth.of(year, month)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        return (0 until yearMonth.lengthOfMonth())
            .map { firstDay.plusDays(it.toLong()) }
            .filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY } }


    @Bean
    fun workplaceLocation(): WorkplaceLocation {
        return workplaceLocationRepository.findById(locationName).get()
    }

    private fun addWorkplaceLocation() {

        if (workplaceLocationRepository.existsById(locationName).not())
            workplaceLocationRepository.save(
                WorkplaceLocation(locationName, Location(
                    BigDecimal( 10.8464752),
                    BigDecimal(106.7784394)))
            )
    }

    private fun addWorkShiftEntries() {
        accountRepository.findAll()
            .forEach {
                if (readonlyWorkShiftRepository.existsByUser(it).not())
                    workShiftRepository.save(WorkShift(
                        it,
                        LocalTime.of(8, 0),
                        LocalTime.of(17, 30)
                    ))
            }
    }

    private fun addTableAuditEntries() {
        listOf(
            ArrivedPackage::class,
            Compartment::class,
            CompartmentProductCategory::class,
            EventCompartment::class,
            FrontCompartment::class,
            InventoryStock::class,
            Layer::class,
            PackageProduct::class,
            Product::class,
            ProductCategorization::class,
            ProductCategory::class,
            ProductOnCompartment::class,

            Event::class,
            EventProduct::class,
        ).forEach {
            if (tableAuditRepository.existsById(TableAudit.toSnakeCase(it.simpleName!!)).not())
                tableAuditRepository.save(TableAudit.fromClass(it))
        }
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


        imageSourceRepository.save(ImageSource(
            UrlValue(
            "https://placehold.co/400?text=Lunch-ables"), lunchableProduct))
    }

    private fun lunchablesDelivery(
        arrivalDate: LocalDateTime = LocalDateTime.of(2024, 6, 6, 6, 0),
        quantity: Int = 10,
    ) {

        val arrivedPackage = ArrivedPackage(
            "Reichtümer",
            arrivalDate
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
            quantity,
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
                UrlValue("https://placehold.co/400?text=${ration.name.replace(' ', '+')}"), ration)
            val waterBottleImg = ImageSource(
                UrlValue("https://placehold.co/400?text=${waterBottle.name.replace(' ', '+')}"), waterBottle)
            val rainCoatImg = ImageSource(
                UrlValue("https://placehold.co/400?text=${rainCoat.name.replace(' ', '+')}"), rainCoat)

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

        val arrivedPackage = ArrivedPackage("Gehirn", LocalDateTime.of(2024, 1, 1, 1, 1))
        val expirationDate = LocalDate.of(2025, 1, 1)

        arrivedPackageRepository.save(arrivedPackage)

        val pps = { i: PackageProduct -> packageProductRepository.save(i)}

        val products = productRepository.findAll()

        val packageProduct1 = PackageProduct(arrivedPackage, products[0], 10, BigDecimal(8), expirationDate)
        val packageProduct2 = PackageProduct(arrivedPackage, products[1], 15, BigDecimal(15), expirationDate)

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
        val arrivedPackage = ArrivedPackage("Gehirn", LocalDateTime.of(2023, 1, 1, 1, 1))
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
        val arrivedPackage = ArrivedPackage("EventCompany", LocalDateTime.of(2024, 4, 1, 1, 1))
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

    private fun addStock(
        arrivedDateTime: LocalDateTime = LocalDateTime.of(2024, 4, 1, 1, 1),
        expirationDate: LocalDate = LocalDate.of(2025, 4, 1),
        quantity: Int = 200
    ) {
        val arrivedPackage = ArrivedPackage("Gehirn", arrivedDateTime)

        arrivedPackageRepository.save(arrivedPackage)

        val pps = { i: PackageProduct -> packageProductRepository.save(i)}

        val ration = productRepository.findById("DK000O").get()
        val waterBottle = productRepository.findById("FD000O").get()
        val rainCoat = productRepository.findById("WP000O").get()

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

    private fun addEventAndNonEventStock(
        arrivedDateTime: LocalDateTime = LocalDateTime.of(2024, 4, 1, 1, 1),
        expirationDate: LocalDate = LocalDate.of(2025, 4, 1),
        quantity: Int = 200) {
        val arrivedPackage = arrivedPackageRepository.save(
            ArrivedPackage("Gehirn", arrivedDateTime)
        )

        fun pps(i: PackageProduct) = packageProductRepository.save(i)

        val ration = productRepository.findById("DK000O").get()
        val waterBottle = productRepository.findById("FD000O").get()

        val packageProduct1 =
            pps(
                PackageProduct(arrivedPackage, ration, quantity, BigDecimal(15), expirationDate)
            )
        val packageProduct2 =
            pps(PackageProduct(arrivedPackage, waterBottle, quantity, BigDecimal(8), expirationDate))

        fun invs (i: InventoryStock) = inventoryRepository.save(i)

        invs(InventoryStock(packageProduct1, quantity))
        invs(InventoryStock(packageProduct2, quantity))
    }

    companion object {
        val locationName = "CircleK"
    }
}