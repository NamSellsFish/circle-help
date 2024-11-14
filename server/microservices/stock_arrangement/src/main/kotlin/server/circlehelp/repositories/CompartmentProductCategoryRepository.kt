package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.CompartmentProductCategory

interface CompartmentProductCategoryRepository : TransactionalJpaRepository<CompartmentProductCategory, Long> {

}