package server.circlehelp.delegated_classes

import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct
import server.circlehelp.services.Logic

class ExpirationStatusArbiter(
    private val logic: Logic,
    private val statusArbiter: StatusArbiter,
) : StatusArbiter {
    override fun decide(
        packageProduct: PackageProduct,
        compartment: Compartment,
        arbitratedStatus: Int
    ): Int {
        return if (logic.isExpiring(packageProduct)) 2 else statusArbiter.decide(packageProduct, compartment, arbitratedStatus)
    }
}