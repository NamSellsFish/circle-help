package server.circlehelp.repositories

import org.springframework.stereotype.Repository
import server.circlehelp.entities.WorkShift
import server.circlehelp.repositories.readonly.ReadonlyRepository

@Repository
interface WorkShiftRepository: TransactionalJpaRepository<WorkShift, Long>