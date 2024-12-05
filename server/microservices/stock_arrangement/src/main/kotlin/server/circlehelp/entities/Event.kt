package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import server.circlehelp.services.TableAuditingService
import java.time.LocalDate

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class Event(

    @NotNull @NotBlank @Size(min = 2, max = 30)
    @Column(length = 30, nullable = false)
    val name: String,

    @NotNull
    @Column(nullable = false)
    val startDate: LocalDate,

    @NotNull
    @Column(nullable = false)
    val endDate: LocalDate,

    @jakarta.persistence.Id @GeneratedValue
    @EqualsAndHashCode.Include val id: Long? = null
) {

    fun asLongRange() : LongRange {
        return startDate.toEpochDay()..endDate.toEpochDay()
    }

    fun isActive() : Boolean {
        return asLongRange().contains(LocalDate.now().toEpochDay())
    }

    fun getActivity(date: LocalDate) : Int {

        if (date < startDate) return date.compareTo(startDate)
        else
        if (date > endDate) return date.compareTo(endDate)
        else
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}