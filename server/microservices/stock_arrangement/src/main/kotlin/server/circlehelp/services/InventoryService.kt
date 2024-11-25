package server.circlehelp.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.entities.Appraisable
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.OrderSubmissionTopic
import server.circlehelp.repositories.AppraisableRepository
import server.circlehelp.repositories.ArrivedPackageRepository
import server.circlehelp.repositories.InventoryRepository
import server.circlehelp.repositories.OrderSubmissionTopicRepository
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
    private val orderSubmissionTopicRepository: OrderSubmissionTopicRepository,
    private val appraisableRepository: AppraisableRepository,

    mapperBuilder: Jackson2ObjectMapperBuilder
) {
    private val objectMapper: ObjectMapper = mapperBuilder.build()

    @RepeatableReadTransaction
    fun submitOrder(submitter: Employee, request: OrderApprovalRequest) {

        val topic = orderSubmissionTopicRepository.save(
            OrderSubmissionTopic(
                objectMapper.writeValueAsString(request)
            )
        )

        val appraisable = appraisableRepository.save(
            Appraisable(
                submitter,
                topic
            )
        )
    }

    @RepeatableReadTransaction
    fun appraiseOrder(appraiser: Admin, appraisable: Appraisable, approved: Boolean, reason: String = "") {
        appraisable.appraise(appraiser, approved, reason)
    }

    @RepeatableReadTransaction
    fun addOrder(orderApprovalRequest: OrderApprovalRequest): ArrivedPackage {

        val order = arrivedPackageRepository.save(orderApprovalRequest.toArrivedPackage())

        for (it in orderApprovalRequest.packageProducts) {

            val product = productRepository.findById(it.sku).get()
            val packageProduct = packageProductRepository.save(it.toPackageProduct(order, product))
            val inventoryStock = inventoryRepository.save(InventoryStock.import(packageProduct))
        }

        return order
    }
}