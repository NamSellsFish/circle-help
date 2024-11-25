package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.Appraisable

@Repository
interface ReadonlyAppraisableRepository : ReadonlyRepository<Appraisable, Long> {
}