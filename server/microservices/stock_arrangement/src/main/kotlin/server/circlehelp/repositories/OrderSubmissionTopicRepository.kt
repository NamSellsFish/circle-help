package server.circlehelp.repositories

import org.springframework.stereotype.Repository
import server.circlehelp.entities.OrderSubmissionTopic

@Repository
interface OrderSubmissionTopicRepository : TransactionalJpaRepository<OrderSubmissionTopic, Long> {
}