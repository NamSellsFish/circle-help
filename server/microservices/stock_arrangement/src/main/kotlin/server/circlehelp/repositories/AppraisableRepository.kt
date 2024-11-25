package server.circlehelp.repositories

import org.springframework.stereotype.Repository
import server.circlehelp.entities.Appraisable

@Repository
interface AppraisableRepository : TransactionalJpaRepository<Appraisable, Long> {
}