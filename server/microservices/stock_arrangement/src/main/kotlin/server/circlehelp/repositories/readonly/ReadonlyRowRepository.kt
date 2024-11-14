package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.Layer

@Repository
interface ReadonlyRowRepository : ReadonlyRepository<Layer, Long> {

    fun findByNumber(number: Int) : Layer?
}