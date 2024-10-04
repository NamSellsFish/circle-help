package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne

@Entity
class ProductOnCompartment(
    @OneToOne var compartment: Compartment,
    @ManyToOne var product: Product,
    @ManyToOne var orderedPackage: ArrivedPackage,
    @Id @GeneratedValue var id: Long? = null
)  {

}