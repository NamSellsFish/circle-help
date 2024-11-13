package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import server.circlehelp.entities.base.IdObjectBase
import java.time.LocalDate

@Entity
class Event(

    @NotNull @NotBlank @Size(min = 2, max = 30)
    @Column(length = 30, nullable = false)
    var name: String,

    @NotNull
    @Column(nullable = false)
    var startDate: LocalDate,

    @NotNull
    @Column(nullable = false)
    var endDate: LocalDate,

    @jakarta.persistence.Id @GeneratedValue
    override var id: Long? = null
) : IdObjectBase<Long>() {

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

}