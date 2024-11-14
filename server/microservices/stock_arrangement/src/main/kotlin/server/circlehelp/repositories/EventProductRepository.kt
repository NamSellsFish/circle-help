package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.entities.EventProduct

@Repository
interface EventProductRepository : TransactionalJpaRepository<EventProduct, Long>