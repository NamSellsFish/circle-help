package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.EventProduct

@Repository
interface ReadonlyEventProductRepository : ReadonlyRepository<EventProduct, Long> {
}