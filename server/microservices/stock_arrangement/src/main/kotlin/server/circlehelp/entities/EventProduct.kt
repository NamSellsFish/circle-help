package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull
import server.circlehelp.entities.base.IdObjectBase

@Entity
class EventProduct(

    @NotNull
    @ManyToOne @JoinColumn(nullable = false)
    var product: Product,

    @NotNull
    @ManyToOne @JoinColumn(nullable = false)
    var event: Event,

    @jakarta.persistence.Id @GeneratedValue
    override var id: Long? = null
): IdObjectBase<Long>() {
}