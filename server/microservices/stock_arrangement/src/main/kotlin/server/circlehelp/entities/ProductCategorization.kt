package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.Id

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "category_id"])])
class ProductCategorization(
    @ManyToOne var product: Product,
    @ManyToOne var category: ProductCategory,
    @jakarta.persistence.Id @GeneratedValue var id: Long? = null
) {
}