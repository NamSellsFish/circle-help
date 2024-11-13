package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.Event
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.Product

@Repository
interface ReadonlyEventProductRepository : ReadonlyRepository<EventProduct, Long> {

    fun findByEventAndProduct(event: Event, product: Product) : EventProduct?

    fun findByProduct(product: Product) : List<EventProduct>

    fun findByEvent(event: Event) : List<EventProduct>
}