package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.WorkplaceLocation

@Repository
interface ReadonlyWorkplaceLocationRepository : ReadonlyRepository<WorkplaceLocation, String> {
}