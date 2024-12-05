package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import server.circlehelp.services.TableAuditingService
import server.circlehelp.value_classes.UrlValue
import java.math.BigDecimal
import java.time.LocalDate


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "ordered_package_id"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class PackageProduct(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val orderedPackage: ArrivedPackage,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val product: Product,

    @NotNull @Min(1)
    @Column(nullable = false)
    var importedQuantity: Int,

    @NotNull @Min(0)
    @Column(nullable = false)
    val wholesalePrice: BigDecimal,

    val expirationDate: LocalDate?,

    @Size(min = 1, max = 255)
    var imageSrc: UrlValue? = null,

    @Size(max = 65_535)
    @Column(nullable = false, columnDefinition = "TEXT")
    var note: String = "",

    @Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
)  {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PackageProduct

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "PackageProduct(orderedPackage=${orderedPackage.id}, product=${product.sku})"
    }

    fun with(importedQuantity: Int? = null,
             note: String? = null): PackageProduct {
        return PackageProduct(
            orderedPackage, product,
            importedQuantity?: this.importedQuantity,
            wholesalePrice, expirationDate,
            imageSrc,
            note ?: this.note
        )
    }

}