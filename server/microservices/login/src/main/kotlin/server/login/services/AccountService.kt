package server.login.services

import org.springframework.security.core.userdetails.UserDetailsService
import server.login.api.response.InboundUserDto
import server.login.entities.User
import java.util.stream.Stream

interface AccountService : UserDetailsService {

    fun saveUser(inboundUserDto: InboundUserDto): User

    fun getUser(username: String): User

    fun listUsers(): Stream<User>

}