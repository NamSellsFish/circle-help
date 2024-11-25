package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.TypedSort
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import server.circlehelp.api.response.AttendanceDto
import server.circlehelp.auth.User
import server.circlehelp.entities.base.IdObjectBase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user", "date"])])
class Attendance
@PersistenceCreator
private constructor (
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val user: User,
    val punchOutTime: LocalTime? = null,
    @jakarta.persistence.Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {

    constructor(user: User) : this(user, null, null)

    @Column(nullable = false)
    val date: LocalDate = LocalDate.now()

    @Column(nullable = false)
    val punchInTime: LocalTime = LocalTime.now()

    fun punchOut() : Attendance {
        return Attendance(user, LocalTime.now(), id)
    }

    companion object {

        val sortByDateDesc = Sort.by(Sort.Order(Sort.Direction.DESC, Attendance::date.name))

    }
}