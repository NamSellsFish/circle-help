package server.circlehelp.repositories.readonly

import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory

@Repository
interface ReadonlyCompartmentProductCategoryRepository : ReadonlyRepository<CompartmentProductCategory, Long> {

    fun findByCompartment(compartment: Compartment) : CompartmentProductCategory?

}