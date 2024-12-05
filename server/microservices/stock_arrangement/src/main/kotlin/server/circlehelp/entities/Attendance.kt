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
import lombok.EqualsAndHashCode
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.TypedSort
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import server.circlehelp.api.response.ArbitratedAttendance
import server.circlehelp.api.response.AttendanceDto
import server.circlehelp.api.response.AttendanceType
import server.circlehelp.auth.User
import server.circlehelp.delegated_classes.AttendanceTypeArbiter
import server.circlehelp.services.TableAuditingService
import server.circlehelp.value_classes.UrlValue
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["user_email", "date"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class Attendance (
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val user: User,
    val date: LocalDate,
    val punchInTime: LocalTime,
    var punchOutTime: LocalTime? = null,
    val imageUrl: UrlValue,
    var type: AttendanceType,
    @jakarta.persistence.Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attendance

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {

        val sortByDateDesc = Sort.by(Sort.Order(Sort.Direction.DESC, Attendance::date.name))

    }
}