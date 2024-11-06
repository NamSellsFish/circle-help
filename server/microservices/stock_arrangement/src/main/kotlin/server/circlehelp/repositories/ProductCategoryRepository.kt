package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ProductCategory

@Repository
interface ProductCategoryRepository : JpaRepository<ProductCategory, String>