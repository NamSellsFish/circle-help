package server.circlehelp.services

import jakarta.persistence.PostPersist
import jakarta.persistence.PostRemove
import jakarta.persistence.PostUpdate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.TableAuditRepository
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Service
class TableAuditingService(
    applicationContext: ApplicationContext,
    private val transactionService: TransactionService
) {

    private val updateFlags: MutableMap<String, Boolean> = ConcurrentHashMap()

    private val tableAuditRepository: TableAuditRepository by lazy {
        applicationContext.getBean<TableAuditRepository>()
    }

    private val log = LoggerFactory.getLogger(TableAuditingService::class.java)

    @PostUpdate
    @PostRemove
    @PostPersist
    fun updateTableAuditOnEntity(entity: Any) = updateTableAudit(entity.javaClass.simpleName)


    fun updateTableAudit(className: String) {

        val auditName = TableAudit.toSnakeCase(className)
        log.info("Table '$auditName' updated.")

        transactionService.requiresNewRollbackOnAny {
            tableAuditRepository.findById(auditName)
                .let {
                    if (it.isPresent) tableAuditRepository.save(it.get().asUpdated())
                    else tableAuditRepository.save(TableAudit.withPascalCase(className))
                }
        }
    }

    fun updateTableAudit(vararg classes: Class<*>) {
        for (it in classes) {
            updateTableAudit(it.simpleName)
        }
    }

    fun updateTableAudit(vararg classes: KClass<*>) {
        for (it in classes) {
            updateTableAudit(it.simpleName!!)
        }
    }

    final inline fun <reified T> updateTableAudit() = updateTableAudit(T::class.simpleName!!)
}