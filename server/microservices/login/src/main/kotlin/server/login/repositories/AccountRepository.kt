package server.login.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.login.entities.User
import server.login.repositories.readonly.ReadonlyAccountRepository
import server.login.repositories.readonly.ReadonlyRepository

@Repository
interface AccountRepository : JpaRepository<User, String>, ReadonlyAccountRepository