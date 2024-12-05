package server.circlehelp.repositories

import org.springframework.stereotype.Repository
import server.circlehelp.entities.TableAudit
import kotlin.jvm.optionals.getOrNull

@Repository
interface TableAuditRepository : TransactionalJpaRepository<TableAudit, String> {
    companion object {
        inline fun <reified T> TableAuditRepository.findByClass(): TableAudit? {
            return findById(TableAudit.toSnakeCase<T>()).getOrNull()
        }
    }
}