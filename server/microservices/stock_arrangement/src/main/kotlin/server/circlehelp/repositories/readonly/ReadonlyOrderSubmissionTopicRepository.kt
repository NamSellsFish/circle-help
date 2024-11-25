package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.entities.OrderSubmissionTopic

@Repository
interface ReadonlyOrderSubmissionTopicRepository : ReadonlyRepository<OrderSubmissionTopic, Long> {
}