package server.circlehelp.entities

import jakarta.persistence.FetchType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.EqualsAndHashCode

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class CompartmentProductCategory(
    @OneToOne @MapsId("compartmentID") val compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val productCategory: ProductCategory,

) {

    @Id
    @EqualsAndHashCode.Include
    val compartmentID: Long = compartment.id!!

}