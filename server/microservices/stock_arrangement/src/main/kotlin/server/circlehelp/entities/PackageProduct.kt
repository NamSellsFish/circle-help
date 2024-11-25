package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import lombok.EqualsAndHashCode
import java.math.BigDecimal
import java.time.LocalDate


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "ordered_package_id"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class PackageProduct(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val orderedPackage: ArrivedPackage,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val product: Product,

    @NotNull @Min(1)
    @Column(nullable = false)
    val importedQuantity: Int,

    @NotNull @Min(0)
    @Column(nullable = false)
    val wholesalePrice: BigDecimal,

    val expirationDate: LocalDate?,

    @Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
)  {

}