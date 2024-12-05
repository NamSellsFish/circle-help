package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.auth.User
import server.circlehelp.entities.WorkShift

@Repository
interface ReadonlyWorkShiftRepository: ReadonlyRepository<WorkShift, String> {

    fun findByUser(user: User): WorkShift?

    fun existsByUser(user: User): Boolean
}