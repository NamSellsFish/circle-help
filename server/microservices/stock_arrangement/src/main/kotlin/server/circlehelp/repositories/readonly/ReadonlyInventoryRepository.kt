package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct

@Repository
@Primary
interface ReadonlyInventoryRepository : ReadonlyRepository<InventoryStock, Long> {

    fun existsByPackageProduct(packageProduct: PackageProduct): Boolean

    fun findByPackageProduct(packageProduct: PackageProduct): InventoryStock?

    fun findByPackageProductOrderedPackage(orderedPackage: ArrivedPackage) : List<InventoryStock>

    fun findAllByOrderByPackageProductExpirationDateDesc(): List<InventoryStock>

    // TODO: Consider replacing with fragmented query.
    @Query("select i.packageProduct.id from InventoryStock i where i.packageProduct.id = :id")
    fun findIdByPackageProductId(id: Long) : Long?

    fun findAllByOrderByPackageProductOrderedPackageDateTimeDescPackageProductOrderedPackageIdDesc(): List<InventoryStock>

    companion object {

        fun ReadonlyInventoryRepository.addTo(packageProduct: PackageProduct, quantity: Int) : InventoryStock {
            return findByPackageProduct(packageProduct).let {
                it?.apply { inventoryQuantity++ } ?: InventoryStock(packageProduct)
            }.apply { inventoryQuantity += quantity - 1 }
        }
    }
}