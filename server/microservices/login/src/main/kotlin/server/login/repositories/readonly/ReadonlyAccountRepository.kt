package server.login.repositories.readonly

import server.login.entities.User

interface ReadonlyAccountRepository : ReadonlyRepository<User, String>