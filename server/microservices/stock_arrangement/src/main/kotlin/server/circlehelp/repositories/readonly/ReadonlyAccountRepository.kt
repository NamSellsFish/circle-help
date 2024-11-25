package server.circlehelp.repositories.readonly

import org.springframework.stereotype.Repository
import server.circlehelp.auth.User

@Repository
interface ReadonlyAccountRepository : ReadonlyRepository<User, String>