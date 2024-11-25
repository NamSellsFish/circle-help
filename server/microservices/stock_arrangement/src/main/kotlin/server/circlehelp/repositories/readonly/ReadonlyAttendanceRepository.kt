package server.circlehelp.repositories.readonly

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import server.circlehelp.auth.User
import server.circlehelp.entities.Attendance
import java.time.LocalDate

@Repository
interface ReadonlyAttendanceRepository : ReadonlyRepository<Attendance, Long> {

    fun findAllByUser(user: User, sort: Sort = Sort.unsorted()) : List<Attendance>

    fun findAllByDate(date: LocalDate = LocalDate.now(), sort: Sort = Sort.unsorted()): List<Attendance>

    fun findByUserAndDate(user: User, date: LocalDate = LocalDate.now()) : Attendance?

    fun existsByUserAndDate(user: User,
                             date: LocalDate = LocalDate.now()) : Boolean
}