package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id

@Entity
class Product(
    var name: String,
    var price: Double,
    @Id @GeneratedValue var id: Long? = null
)