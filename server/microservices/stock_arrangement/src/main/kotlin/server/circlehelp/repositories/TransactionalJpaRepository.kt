package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import server.circlehelp.annotations.RepeatableReadTransaction


@RepeatableReadTransaction
@NoRepositoryBean
interface TransactionalJpaRepository<T, ID> : JpaRepository<T, ID> {
}