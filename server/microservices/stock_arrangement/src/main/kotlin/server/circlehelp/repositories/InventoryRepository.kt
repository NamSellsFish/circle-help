package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct

@Repository
interface InventoryRepository : TransactionalJpaRepository<InventoryStock, Long> {

    @Modifying
    @RepeatableReadTransaction
    @Query("update InventoryStock i set i.inventoryQuantity = i.inventoryQuantity + 1 where i.id = :id")
    fun incrementQuantityById(id: Long)
}