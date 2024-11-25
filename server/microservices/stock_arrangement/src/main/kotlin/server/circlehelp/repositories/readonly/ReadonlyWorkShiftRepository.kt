package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.auth.User
import server.circlehelp.entities.WorkShift

@Repository
interface ReadonlyWorkShiftRepository: ReadonlyRepository<WorkShift, Long> {

    fun findByUser(user: User): WorkShift?
}