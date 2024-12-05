package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.delegated_classes.TableWatcher
import server.circlehelp.entities.TableAudit
import server.circlehelp.repositories.TableAuditRepository
import java.time.Clock
import kotlin.reflect.KClass

@Service
class TableWatcherBuilderService(
    private val tableAuditRepository: TableAuditRepository,
    private val transactionService: TransactionService,
    private val clock: Clock
) {

    fun <T> fromClasses(classes: Iterable<KClass<*>>,
                        updateEvent: () -> T) : TableWatcher<T> {

        return fromPascalCase(classes.map {it.simpleName!!}, updateEvent)
    }

    fun <T> fromPascalCase(classNames: Iterable<String>,
                           updateEvent: () -> T) : TableWatcher<T> {

        return fromSnakeCase(classNames.map { TableAudit.toSnakeCase(it) }, updateEvent)
    }

    fun <T> fromSnakeCase(tableNames: Iterable<String>,
                          updateEvent: () -> T) : TableWatcher<T> {

        return TableWatcher.fromSnakeCase(tableNames, updateEvent, tableAuditRepository, transactionService, clock)
    }

}