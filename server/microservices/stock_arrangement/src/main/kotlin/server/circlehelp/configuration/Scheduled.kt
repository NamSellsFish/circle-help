package server.circlehelp.configuration

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import server.circlehelp.repositories.ProductOnCompartmentRepository
import server.circlehelp.services.ActiveEventsService
import server.circlehelp.services.Logic
import server.circlehelp.services.StatusArbiterManager
import server.circlehelp.services.TransactionService

@Configuration
@EnableScheduling
class Scheduled(
    private val readonlyProductOnCompartmentRepository: ProductOnCompartmentRepository,

    private val productOnCompartmentRepository: ProductOnCompartmentRepository,

    private val activeEventsService: ActiveEventsService,
    private val logic: Logic,
    private val transactionService: TransactionService,
    statusArbiterManager: StatusArbiterManager,
) {

    private val statusArbiter = statusArbiterManager.eventStatusArbiter
    private val logger = LoggerFactory.getLogger(Scheduled::class.java)

    @Scheduled(initialDelay = 0)
    @Scheduled(cron = "0 0 0 * * *")
    fun dailyUpdate() {
        transactionService.requiredRollbackOnAny {
            updateExpirationStatus()
            updateEventStatus()
        }
        logger.info("Ran daily scheduled updates.")
    }

    fun updateExpirationStatus() {
        transactionService.requiredRollbackOnAny {
            readonlyProductOnCompartmentRepository.findAll().map { logic.updateExpiration(it) }
        }
    }

    fun updateEventStatus() {
        transactionService.requiredRollbackOnAny {
            activeEventsService.refresh()
            readonlyProductOnCompartmentRepository.findAll()
                .map { statusArbiter.update(it).let { productOnCompartmentRepository.save(it) } }
        }
    }
}