package server.circlehelp.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@Entity
class ProductOnCompartment(
    @OneToOne var compartment: Compartment,
    @ManyToOne var packageProduct: PackageProduct,

    @NotNull @Min(0)
    @Column(nullable = false)
    var status: Int = 0,

    @Id @GeneratedValue var id: Long? = null
)  {

}