package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.Id
import jakarta.persistence.FetchType
import lombok.EqualsAndHashCode

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "category_id"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ProductCategorization(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val product: Product,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val category: ProductCategory,
    @jakarta.persistence.Id @GeneratedValue
    @EqualsAndHashCode.Include val id: Long? = null
) {
}