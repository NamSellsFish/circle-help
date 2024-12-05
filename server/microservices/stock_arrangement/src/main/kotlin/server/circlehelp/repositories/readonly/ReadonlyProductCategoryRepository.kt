package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ProductCategory

@Repository
@Primary
interface ReadonlyProductCategoryRepository: ReadonlyRepository<ProductCategory, String> {

    fun findByName(name: String): ProductCategory?
}