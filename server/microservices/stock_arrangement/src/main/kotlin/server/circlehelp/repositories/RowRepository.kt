package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Layer
import server.circlehelp.entities.Shelf

@Repository
interface RowRepository : JpaRepository<Layer, Long> {
    fun findByShelfAndNumber(shelf: Shelf, number: Int) : Layer?
}