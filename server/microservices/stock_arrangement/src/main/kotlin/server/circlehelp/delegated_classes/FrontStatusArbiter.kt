package server.circlehelp.delegated_classes

import org.slf4j.LoggerFactory
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct
import server.circlehelp.repositories.caches.FrontCompartmentCache

class FrontStatusArbiter(
    private val frontCompartmentCache: FrontCompartmentCache,
    private val statusArbiter: StatusArbiter,
) : StatusArbiter {

    private val logger = LoggerFactory.getLogger(FrontStatusArbiter::class.java)

    override fun decide(
        packageProduct: PackageProduct,
        compartment: Compartment,
        arbitratedStatus: Int
    ): Int {
        return if (frontCompartmentCache.existsByCompartment(compartment) && arbitratedStatus == 3) {
            logger.warn("Unexpected product ${packageProduct.product.sku} package ${packageProduct.orderedPackage.id}" +
                    " status 3 at front compartment ${compartment.getLocation()}")
            throw IllegalArgumentException("Unexpected product ${packageProduct.product.sku} package ${packageProduct.orderedPackage.id}" +
                    " status 3 at front compartment ${compartment.getLocation()}")
        }
        else
            statusArbiter.decide(packageProduct, compartment, arbitratedStatus)
    }

}