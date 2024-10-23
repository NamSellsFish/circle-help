package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import server.circlehelp.entities.EventCompartment

interface EventCompartmentRepository : JpaRepository<EventCompartment, Long> {
}