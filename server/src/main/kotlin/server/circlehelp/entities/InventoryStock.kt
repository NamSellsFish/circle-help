package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product", "orderedPackage"])])
class InventoryStock(
    @Id @GeneratedValue var id: Long? = null,
    @ManyToOne var product: Product,
    @ManyToOne var orderedPackage: ArrivedPackage,
    var inventoryQuantity: Int
)