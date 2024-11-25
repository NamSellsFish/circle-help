package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull
import server.circlehelp.entities.base.IdObjectBase

@Entity
class InventoryStock(
    @OneToOne @MapsId("id") var packageProduct: PackageProduct,

    @NotNull @Min(1)
    @Column(nullable = false)
    var inventoryQuantity: Int = 1,

) : IdObjectBase<Long>()  {

    @Id @JoinColumn(name = "package_product_id") override val id: Long = packageProduct.id!!

    companion object {
        fun import(packageProduct: PackageProduct) = InventoryStock(packageProduct, packageProduct.importedQuantity)
    }
}