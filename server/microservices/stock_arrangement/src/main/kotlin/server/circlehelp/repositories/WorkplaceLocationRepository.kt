package server.circlehelp.repositories

import org.springframework.stereotype.Repository
import server.circlehelp.entities.WorkplaceLocation
import server.circlehelp.services.TransactionService

@Repository
interface WorkplaceLocationRepository : TransactionalJpaRepository<WorkplaceLocation, String> {
}