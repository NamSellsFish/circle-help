package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.EventCompartment

@Repository
interface ReadonlyEventCompartmentRepository : ReadonlyRepository<EventCompartment, Long> {

    fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<EventCompartment>
}