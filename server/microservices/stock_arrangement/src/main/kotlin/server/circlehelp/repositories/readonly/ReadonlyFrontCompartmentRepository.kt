package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.FrontCompartment

@Repository
@Primary
interface ReadonlyFrontCompartmentRepository : ReadonlyRepository<FrontCompartment, Long> {

    fun existsByCompartment(compartment: Compartment) : Boolean

    fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<FrontCompartment>
}