package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.persistence.FetchType
import lombok.EqualsAndHashCode

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ProductOnCompartment(
    @OneToOne @MapsId("compartmentID") var compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var packageProduct: PackageProduct,

    @NotNull @Min(0)
    @Column(nullable = false)
    var status: Int = 1,
) {

    @Id val compartmentID: Long = compartment.id!!
}