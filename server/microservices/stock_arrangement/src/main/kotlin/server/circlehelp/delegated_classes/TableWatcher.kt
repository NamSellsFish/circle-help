package server.circlehelp.delegated_classes

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.TableAuditRepository
import server.circlehelp.services.TransactionService
import java.time.Clock
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass

class TableWatcher<T> protected constructor(
    private val tableNames: Iterable<String>,
    private val updateEvent: () -> T,
    private val tableAuditRepository: TableAuditRepository,
    private val transactionService: TransactionService,
    private val clock: Clock,
) {
    var updatedTime: LocalDateTime? = null
        protected set

    private val log = LoggerFactory.getLogger(TableWatcher::class.java)

    @ManagementRequiredTransaction(readOnly = true)
    @Throws(EmptyResultDataAccessException::class)
    fun checkTables(): T? {

        return transactionService.managementRequiredReadonly {
            for(it in tableNames) {
                val audit = tableAuditRepository.findById(it).getOrNull()
                    ?: throw NoSuchElementException(
                        "Record for table '$it' in the 'table_audit' table not found.\n" +
                        "TableWatcher is designed to be readonly.\n" +
                        "One must be added in the 'table_audit' table before running the server.")

                if (updatedTime == null || updatedTime!! < audit.updatedDate) {

                    if (updatedTime == null)
                        log.info("Initializing watcher for table '$it'")
                    else
                        log.info("Table '$it' has changed.")
                    updatedTime = LocalDateTime.now(clock)
                    return@managementRequiredReadonly updateEvent()
                }

                /*
                if (updatedTime!! >= audit.updatedDate) {
                    log.info("Table '$it' unchanged.")
                }
                 */
            }

            return@managementRequiredReadonly null
        }
    }

    companion object {

        fun <T> fromClasses(classes: Iterable<KClass<*>>,
                            updateEvent: () -> T,
                            tableAuditRepository: TableAuditRepository,
                            transactionService: TransactionService,
                            clock: Clock) : TableWatcher<T> {

            return fromPascalCase(classes.map {it.simpleName!!}, updateEvent, tableAuditRepository, transactionService, clock)
        }

        fun <T> fromPascalCase(classNames: Iterable<String>,
                           updateEvent: () -> T,
                           tableAuditRepository: TableAuditRepository,
                           transactionService: TransactionService,
                               clock: Clock) : TableWatcher<T> {

            return fromSnakeCase(classNames.map { TableAudit.toSnakeCase(it) }, updateEvent, tableAuditRepository, transactionService, clock)
        }

        fun <T> fromSnakeCase(tableNames: Iterable<String>,
                           updateEvent: () -> T,
                           tableAuditRepository: TableAuditRepository,
                          transactionService: TransactionService,
                              clock: Clock) : TableWatcher<T> {

            return TableWatcher(tableNames, updateEvent, tableAuditRepository, transactionService, clock)
        }
    }
}