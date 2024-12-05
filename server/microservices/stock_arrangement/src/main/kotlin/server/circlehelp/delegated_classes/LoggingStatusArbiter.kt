package server.circlehelp.delegated_classes

import org.slf4j.LoggerFactory
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct

class LoggingStatusArbiter(private val statusArbiter: StatusArbiter): StatusArbiter {

    private val logger = LoggerFactory.getLogger(LoggingStatusArbiter::class.java)

    override fun decide(
        packageProduct: PackageProduct,
        compartment: Compartment,
        arbitratedStatus: Int
    ): Int {
        logger.info("Arbitrated status: $arbitratedStatus")
        return statusArbiter.decide(packageProduct, compartment, arbitratedStatus)
            .also { logger.info("Arbitration decision of product ${packageProduct.product.sku} of package ${packageProduct.orderedPackage.id}" +
                    " at compartment ${compartment.getLocation()} is $it") }
    }
}