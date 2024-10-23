package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.UniqueConstraint
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate


@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product", "orderedPackage"])])
class PackageProduct(
    @ManyToOne var orderedPackage: ArrivedPackage,
    @ManyToOne var product: Product,
    var importedQuantity: Int,
    var wholesalePrice: Double,
    var expirationDate: LocalDate?,
    @Id @GeneratedValue var id: Long? = null
) {

}