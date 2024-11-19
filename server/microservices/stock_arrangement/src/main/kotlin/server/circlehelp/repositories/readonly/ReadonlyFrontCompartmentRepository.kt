package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.FrontCompartment

@Repository
interface ReadonlyFrontCompartmentRepository : ReadonlyRepository<FrontCompartment, Long> {

    fun findAllByOrderByCompartmentNumberAscCompartmentLayerNumberAsc(): List<FrontCompartment>
}