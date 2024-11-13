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

}