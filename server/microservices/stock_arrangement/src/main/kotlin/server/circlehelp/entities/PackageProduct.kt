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
import server.circlehelp.entities.base.IdObjectBase
import java.math.BigDecimal
import java.time.LocalDate


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "ordered_package_id"])])
class PackageProduct(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var orderedPackage: ArrivedPackage,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var product: Product,

    @NotNull @Min(1)
    @Column(nullable = false)
    var importedQuantity: Int,

    @NotNull @Min(0)
    @Column(nullable = false)
    var wholesalePrice: BigDecimal,

    var expirationDate: LocalDate?,

    @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>()  {

}