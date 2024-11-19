package server.circlehelp.repositories.readonly

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct

@Repository
interface ReadonlyInventoryRepository : ReadonlyRepository<InventoryStock, Long> {

    fun findByPackageProduct(packageProduct: PackageProduct): InventoryStock?

    fun findAllByOrderByPackageProductExpirationDateDesc(): List<InventoryStock>

    // TODO: Consider replacing with fragmented query.
    @Query("SELECT id FROM circle_help_db.inventory_stock WHERE package_product_id = :id;", nativeQuery = true)
    fun findIdByPackageProductId(id: Long) : Long?

    fun findAllByOrderByPackageProductOrderedPackageDateDescPackageProductOrderedPackageIdDesc(): List<InventoryStock>

    companion object {

        fun ReadonlyInventoryRepository.addTo(packageProduct: PackageProduct, quantity: Int) : InventoryStock {
            return  findByPackageProduct(packageProduct).let {
                it?.apply { inventoryQuantity++ } ?: InventoryStock(packageProduct)
            }.apply { inventoryQuantity += quantity - 1 }
        }
    }
}