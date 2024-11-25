package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.entities.Attendance

@Repository
interface AttendanceRepository : TransactionalJpaRepository<Attendance, Long> {
}