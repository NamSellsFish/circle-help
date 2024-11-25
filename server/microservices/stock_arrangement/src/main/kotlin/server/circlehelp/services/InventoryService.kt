package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.PackageProductRepository
import server.circlehelp.repositories.ProductRepository
import server.circlehelp.repositories.readonly.ReadonlyArrivedPackageRepository
import server.circlehelp.repositories.readonly.ReadonlyInventoryRepository
import server.circlehelp.repositories.readonly.ReadonlyPackageProductRepository

@Service
class InventoryService(
    private val readonlyInventoryRepository: ReadonlyInventoryRepository,
    private val readonlyPackageProductRepository: ReadonlyPackageProductRepository,
    private val readonlyArrivedPackageRepository: ReadonlyArrivedPackageRepository,
    private val productRepository: ProductRepository,

    private val inventoryRepository: InventoryRepository,
    private val packageProductRepository: PackageProductRepository,
    private val arrivedPackageRepository: ArrivedPackageRepository,
) {

    @RepeatableReadTransaction
    fun addOrder(orderApprovalRequest: OrderApprovalRequest) : ArrivedPackage {

        val order = arrivedPackageRepository.save(orderApprovalRequest.toArrivedPackage())

        for (it in orderApprovalRequest.packageProducts) {

            val product = productRepository.findById(it.sku).get()
            val packageProduct = packageProductRepository.save(it.toPackageProduct(order, product))
            val inventoryStock = inventoryRepository.save(InventoryStock.import(packageProduct))
        }

        return order
    }
}