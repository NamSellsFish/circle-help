package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import server.circlehelp.entities.FrontCompartment

interface FrontCompartmentRepository : JpaRepository<FrontCompartment, Long> {
}