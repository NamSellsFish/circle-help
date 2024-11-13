package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.Event

@Repository
interface ReadonlyEventRepository : ReadonlyRepository<Event, Long> {
}