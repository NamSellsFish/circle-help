package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.validation.constraints.Min
import lombok.EqualsAndHashCode
import org.jetbrains.annotations.NotNull

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class InventoryStock(
    @OneToOne @MapsId("packageProductID") var packageProduct: PackageProduct,

    @NotNull @Min(1)
    @Column(nullable = false)
    var inventoryQuantity: Int = 1,

) {

    @Id @EqualsAndHashCode.Include
    val packageProductID: Long = packageProduct.id!!

    companion object {
        fun import(packageProduct: PackageProduct) = InventoryStock(packageProduct, packageProduct.importedQuantity)
    }
}