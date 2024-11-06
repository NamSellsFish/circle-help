package server.circlehelp.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull

@Entity
class InventoryStock(
    @OneToOne var packageProduct: PackageProduct,

    @NotNull @Min(0)
    @Column(nullable = false)
    var inventoryQuantity: Int,

    @Id @GeneratedValue var id: Long? = null
) {
}