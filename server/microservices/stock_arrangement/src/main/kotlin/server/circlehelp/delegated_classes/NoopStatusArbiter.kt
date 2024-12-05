package server.circlehelp.delegated_classes

import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct

object NoopStatusArbiter : StatusArbiter {
    override fun decide(
        packageProduct: PackageProduct,
        compartment: Compartment,
        arbitratedStatus: Int
    ): Int = arbitratedStatus
}