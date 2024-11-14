package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.Layer

@Repository
interface CompartmentRepository : TransactionalJpaRepository<Compartment, Long> {
}