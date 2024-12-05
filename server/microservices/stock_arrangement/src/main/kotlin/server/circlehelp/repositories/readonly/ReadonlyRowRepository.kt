package server.circlehelp.repositories.readonly

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import server.circlehelp.entities.Layer

@Repository
@Primary
interface ReadonlyRowRepository : ReadonlyRepository<Layer, Long> {

    fun findByNumber(number: Int) : Layer?
}