package server.circlehelp.entities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import server.circlehelp.api.request.OrderApprovalRequest
import server.circlehelp.services.InventoryService

@Entity
class OrderSubmissionTopic(
    @Column(nullable = false, columnDefinition = "TEXT")
    val json: String,
    id: Long? = null,
) : Topic(id) {

    @ManyToOne(fetch = FetchType.LAZY)
    var order: ArrivedPackage? = null
        protected set

    @Throws(IllegalStateException::class)
    override fun submit(applicationContext: ApplicationContext): ArrivedPackage {

        if (order != null) throw IllegalStateException("Already submitted.")

        val inventoryService = applicationContext.getBean<InventoryService>()
        val objectMapper = applicationContext.getBean<Jackson2ObjectMapperBuilder>()
            .build<ObjectMapper>()

        val arrivedPackage = inventoryService.addOrder(objectMapper.readValue<OrderApprovalRequest>(json))

        order = arrivedPackage

        return arrivedPackage
    }

}