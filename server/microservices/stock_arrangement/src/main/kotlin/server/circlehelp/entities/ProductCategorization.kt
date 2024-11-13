package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.Id
import server.circlehelp.entities.base.IdObjectBase

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "category_id"])])
class ProductCategorization(
    @ManyToOne @JoinColumn(nullable = false) var product: Product,
    @ManyToOne @JoinColumn(nullable = false) var category: ProductCategory,
    @jakarta.persistence.Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {
}