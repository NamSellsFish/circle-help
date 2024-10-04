package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.ProductOnCompartment

@Repository
interface ProductOnCompartmentRepository : JpaRepository<ProductOnCompartment, Long> {


    fun findByCompartment(compartment: Compartment) : ProductOnCompartment?
}