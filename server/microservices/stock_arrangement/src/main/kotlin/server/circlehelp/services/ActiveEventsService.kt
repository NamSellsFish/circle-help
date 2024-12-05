package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.delegated_classes.TableWatcher
import server.circlehelp.entities.Event
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.TableAuditRepository
import server.circlehelp.repositories.TableAuditRepository.Companion.findByClass
import java.time.LocalDateTime

@Service
class ActiveEventsService(
    private val logic: Logic,
    tableWatcherBuilderService: TableWatcherBuilderService
) {
    private val tableWatcher = tableWatcherBuilderService.fromClasses(
        listOf(Event::class, EventProduct::class, Product::class),
        ::refresh
    )

    @ManagementRequiredTransaction
    fun refresh() {
        val newValue = updatedValue()
        activeEventProductsSet = newValue
        activeProductsSet = newValue.map { it.product }.toSet()
        activeProductsSkuSet = newValue.map { it.product.sku }.toSet()
    }

    private fun updatedValue(): Set<EventProduct> {
        return logic.activeEventProducts().toSet()
    }

    @get:ManagementRequiredTransaction
    var activeEventProductsSet: Set<EventProduct> = emptySet()
        get() {
            tableWatcher.checkTables()
            return field
        }
        protected set

    @get:ManagementRequiredTransaction
    var activeProductsSet: Set<Product> = emptySet()
        get() {
            tableWatcher.checkTables()
            return field
        }
        protected set

    @get:ManagementRequiredTransaction
    var activeProductsSkuSet: Set<String> = emptySet()
        get() {
            tableWatcher.checkTables()
            return field
        }
        protected set
}