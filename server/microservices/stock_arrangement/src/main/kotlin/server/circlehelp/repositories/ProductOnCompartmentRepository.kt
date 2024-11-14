package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.ProductOnCompartment

@Repository
interface ProductOnCompartmentRepository : TransactionalJpaRepository<ProductOnCompartment, Long> {

    fun deleteByCompartment(compartment: Compartment)

    /*
    fun overwrite(productOnCompartment: ProductOnCompartment) : ProductOnCompartment {

        val previousCompartment = findByCompartment(productOnCompartment.compartment)
            ?: return save(productOnCompartment)

        previousCompartment.status = productOnCompartment.status
        previousCompartment.packageProduct = productOnCompartment.packageProduct

        return save(previousCompartment)
    }

     */
}