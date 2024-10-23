package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct

@Repository
interface InventoryRepository : JpaRepository<InventoryStock, Long> {

    fun findByPackageProduct(packageProduct: PackageProduct): InventoryStock?

    fun findAllByOrderByPackageProductExpirationDateDesc(): List<InventoryStock>
}