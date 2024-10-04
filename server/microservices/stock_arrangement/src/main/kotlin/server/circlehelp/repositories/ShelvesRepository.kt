package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Shelf

@Repository
interface ShelvesRepository : JpaRepository<Shelf, Long> {
    fun findByNumber(number: Int): Shelf?
}