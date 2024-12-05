package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.EventCompartment

@Repository
@Primary
interface ReadonlyEventCompartmentRepository : ReadonlyRepository<EventCompartment, Long> {

    fun existsByCompartment(compartment: Compartment) : Boolean

    fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<EventCompartment>
}