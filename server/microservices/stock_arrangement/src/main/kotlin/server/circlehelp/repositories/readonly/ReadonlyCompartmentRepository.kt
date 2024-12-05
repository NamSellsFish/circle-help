package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.Layer

@Repository
@Primary
interface ReadonlyCompartmentRepository : ReadonlyRepository<Compartment, Long> {

    fun findByLayerAndNumber(layer: Layer, number: Int) : Compartment?

    fun findAllByOrderByNumberAscLayerNumberAsc(): List<Compartment>

    fun findAllByOrderByLayerNumberAscNumberAsc(): List<Compartment>
}