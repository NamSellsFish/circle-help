package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
class CompartmentProductCategory(
    @OneToOne var compartment: Compartment,
    @ManyToOne var productCategory: ProductCategory,
    @jakarta.persistence.Id @GeneratedValue
    var id: Long? = null,
) {
}