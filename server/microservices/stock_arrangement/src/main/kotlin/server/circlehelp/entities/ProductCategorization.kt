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
import jakarta.persistence.FetchType

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["product_sku", "category_id"])])
class ProductCategorization(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var product: Product,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var category: ProductCategory,
    @jakarta.persistence.Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {
}