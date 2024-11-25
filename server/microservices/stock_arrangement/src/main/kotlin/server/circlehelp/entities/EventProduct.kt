package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotNull
import lombok.EqualsAndHashCode

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "product_sku"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class EventProduct(

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val product: Product,

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val event: Event,

    @jakarta.persistence.Id @GeneratedValue
    @EqualsAndHashCode.Include val id: Long? = null
) {
}