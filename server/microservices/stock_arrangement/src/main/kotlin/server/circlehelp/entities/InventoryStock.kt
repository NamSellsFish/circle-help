package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
class InventoryStock(
    @OneToOne var packageProduct: PackageProduct,
    var inventoryQuantity: Int,
    @Id @GeneratedValue var id: Long? = null
) {
}