package server.login.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import server.login.entities.User

@Repository
interface AccountRepository : JpaRepository<User, String>