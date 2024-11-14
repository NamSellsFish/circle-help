package server.circlehelp.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.repositories.TransactionalJpaRepository

@Repository
@RepeatableReadTransaction(readOnly = true)
interface AccountRepository : TransactionalJpaRepository<User, String>