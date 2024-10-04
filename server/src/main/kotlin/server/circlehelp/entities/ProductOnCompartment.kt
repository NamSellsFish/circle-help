package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne

@Entity
class ProductOnCompartment(
    @Id @GeneratedValue var id: Long? = null,
    @OneToOne var compartment: Compartment,
    @ManyToOne var product: Product
)