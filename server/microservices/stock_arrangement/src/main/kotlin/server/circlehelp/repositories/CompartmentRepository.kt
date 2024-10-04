package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Compartment
import server.circlehelp.entities.Layer
import server.circlehelp.entities.Shelf

@Repository
interface CompartmentRepository : JpaRepository<Compartment, Long> {
    fun findByLayerAndNumber(layer: Layer, number: Int) : Compartment?
}