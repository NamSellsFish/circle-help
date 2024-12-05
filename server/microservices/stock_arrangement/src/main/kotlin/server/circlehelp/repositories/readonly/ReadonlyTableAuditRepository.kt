package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.TableAudit

@Repository
interface ReadonlyTableAuditRepository: ReadonlyRepository<TableAudit, String>