package server.circlehelp.repositories

import jakarta.persistence.ManyToOne
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.api.response.InventoryStockItem
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.Product

@Repository
interface InventoryRepository : JpaRepository<InventoryStock, Long> {

    fun findByProductAndOrderedPackage(product: Product,
                           orderedPackage: ArrivedPackage): InventoryStock?
}