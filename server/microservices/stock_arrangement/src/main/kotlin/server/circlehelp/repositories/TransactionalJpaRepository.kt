package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.annotations.RepeatableReadTransaction


@NoRepositoryBean
@ManagementRequiredTransaction
interface TransactionalJpaRepository<T, ID> : JpaRepository<T, ID> {
}