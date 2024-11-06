package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "ordered_package_id"])])
class PackageProduct(
    @ManyToOne var orderedPackage: ArrivedPackage,
    @ManyToOne var product: Product,

    @NotNull @Min(1)
    @Column(nullable = false)
    var importedQuantity: Int,

    @NotNull @Min(0)
    @Column(nullable = false)
    var wholesalePrice: BigDecimal,

    var expirationDate: LocalDate?,


    @Id @GeneratedValue var id: Long? = null
) {

}