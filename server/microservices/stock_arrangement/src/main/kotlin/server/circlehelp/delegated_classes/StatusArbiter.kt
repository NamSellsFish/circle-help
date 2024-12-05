package server.circlehelp.delegated_classes

import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.ProductOnCompartment

@ManagementRequiredTransaction(readOnly = true)
interface StatusArbiter {

    fun decide(packageProduct: PackageProduct, compartment: Compartment, arbitratedStatus: Int = 1): Int

    fun decide(productOnCompartment: ProductOnCompartment): Int {
        return decide(productOnCompartment.packageProduct, productOnCompartment.compartment, productOnCompartment.status)
    }

    fun update(productOnCompartment: ProductOnCompartment): ProductOnCompartment {
        val status = decide(productOnCompartment)
        return productOnCompartment.let {
            if (status != it.status) it.with(status = status) else it
        }
    }
}