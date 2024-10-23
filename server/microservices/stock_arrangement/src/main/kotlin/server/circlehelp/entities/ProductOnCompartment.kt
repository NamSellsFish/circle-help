package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne

@Entity
class ProductOnCompartment(
    @OneToOne var compartment: Compartment,
    @ManyToOne var packageProduct: PackageProduct,
    var status: Int = 0,
    @Id @GeneratedValue var id: Long? = null
)  {

}