package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotNull
import server.circlehelp.entities.base.IdObjectBase

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "product_sku"])])
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