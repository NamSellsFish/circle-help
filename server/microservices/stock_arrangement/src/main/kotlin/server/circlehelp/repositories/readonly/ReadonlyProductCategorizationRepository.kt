package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.ProductCategory

@Repository
@Primary
interface ReadonlyProductCategorizationRepository : ReadonlyRepository<ProductCategorization, Long> {
    fun findAllByProduct(product: Product) : List<ProductCategorization>

    fun findAllByCategory(category: ProductCategory) : List<ProductCategorization>

    fun findByProductAndCategory(product: Product, category: ProductCategory): ProductCategorization?

    fun existsByProductAndCategory(product: Product, category: ProductCategory): Boolean
}