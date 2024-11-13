package server.circlehelp.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull
import server.circlehelp.entities.base.IdObjectBase

@Entity
class InventoryStock(
    @OneToOne @JoinColumn(nullable = false) var packageProduct: PackageProduct,

    @NotNull @Min(1)
    @Column(nullable = false)
    var inventoryQuantity: Int = 1,

    @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>()  {
}