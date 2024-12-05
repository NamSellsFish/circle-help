package server.circlehelp.delegated_classes

import org.slf4j.LoggerFactory
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.ProductOnCompartment
import server.circlehelp.repositories.caches.EventCompartmentCache
import server.circlehelp.services.ActiveEventsService

class EventStatusArbiter(
    private val eventCompartmentCache: EventCompartmentCache,
    private val activeEventsService: ActiveEventsService,
    private val statusArbiter: StatusArbiter,
) : StatusArbiter {

    private val logger = LoggerFactory.getLogger(EventStatusArbiter::class.java)

    override fun decide(packageProduct: PackageProduct, compartment: Compartment, arbitratedStatus: Int): Int {

        logger.info("Is Event Compartment: ${eventCompartmentCache.existsByCompartment(compartment)}")
        logger.info("Is Active Event Product: ${activeEventsService.activeProductsSkuSet.contains(packageProduct.product.sku)}")

        val isEventCompartment = eventCompartmentCache.existsByCompartment(compartment)

        return if (isEventCompartment ||
            activeEventsService.activeProductsSkuSet.contains(packageProduct.product.sku)
                .not())
            if (isEventCompartment)
                1
            else
                statusArbiter.decide(packageProduct, compartment, arbitratedStatus)
        else 4
    }

    override fun update(productOnCompartment: ProductOnCompartment): ProductOnCompartment {
        return if (eventCompartmentCache.existsByCompartment(productOnCompartment.compartment) ||
            //activeEventsService.activeProductsSet.contains(productOnCompartment.packageProduct.product)
            activeEventsService.activeProductsSkuSet.contains(productOnCompartment.packageProduct.product.sku)
                .not())
            statusArbiter.update(productOnCompartment)
        else
            productOnCompartment.with(status = 4)
    }
}