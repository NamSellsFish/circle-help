package server.circlehelp.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import server.circlehelp.entities.base.IdObjectBase
import jakarta.persistence.FetchType

@Entity
class ProductOnCompartment(
    @OneToOne @MapsId("id") var compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var packageProduct: PackageProduct,

    @NotNull @Min(0)
    @Column(nullable = false)
    var status: Int = 1,
) : IdObjectBase<Long>() {

    @Id @JoinColumn(name = "compartment_id") override val id: Long = compartment.id!!
}