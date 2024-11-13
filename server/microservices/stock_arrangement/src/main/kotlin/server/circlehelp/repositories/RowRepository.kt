package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Layer

@Repository
interface RowRepository : JpaRepository<Layer, Long> {
    fun findByNumber(number: Int) : Layer?
}