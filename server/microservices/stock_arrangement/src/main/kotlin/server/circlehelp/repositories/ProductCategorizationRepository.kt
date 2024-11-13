package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization

interface ProductCategorizationRepository : JpaRepository<ProductCategorization, Long> {
}