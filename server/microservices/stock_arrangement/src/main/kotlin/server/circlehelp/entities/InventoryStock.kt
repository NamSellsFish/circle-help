package server.circlehelp.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Version
import jakarta.validation.constraints.Min
import lombok.EqualsAndHashCode
import lombok.With
import org.jetbrains.annotations.NotNull
import org.springframework.data.domain.Sort
import server.circlehelp.services.TableAuditingService

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//@EntityListeners(TableAuditingService::class)
class InventoryStock(
    @OneToOne(optional = false, cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @MapsId("packageProductID")
    val packageProduct: PackageProduct,

    @NotNull @Min(1)
    @Column(nullable = false)
    var inventoryQuantity: Int = 1,

    @Version
    val version: Int = 0
) {

    @Id @EqualsAndHashCode.Include
    val packageProductID: Long = packageProduct.id!!

    companion object {
        fun import(packageProduct: PackageProduct) = InventoryStock(packageProduct, packageProduct.importedQuantity)

        val expirationDateDescSort = Sort.by(Sort.Order(Sort.Direction.DESC,
            "${InventoryStock::packageProduct.name}.${PackageProduct::expirationDate.name}"))

        val packageProductOrderDateTimeSort = Sort.by(
            "${InventoryStock::packageProduct.name}.${PackageProduct::orderedPackage.name}.${ArrivedPackage::dateTime.name}"
        )

        val packageProductOrderIdSort = Sort.by(
            "${InventoryStock::packageProduct.name}.${PackageProduct::orderedPackage.name}.${ArrivedPackage::id.name}"
        )

        val newestImportSort = packageProductOrderDateTimeSort.descending()
            .and(packageProductOrderIdSort.descending())
    }

    fun with(packageProduct: PackageProduct? = null,
             inventoryQuantity: Int? = null,
             version: Int? = null): InventoryStock {
        return InventoryStock(
            packageProduct ?: this.packageProduct,
            inventoryQuantity ?: this.inventoryQuantity,
            version ?: this.version
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InventoryStock

        return packageProductID == other.packageProductID
    }

    override fun hashCode(): Int {
        return packageProductID.hashCode()
    }
}