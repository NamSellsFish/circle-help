package server.login.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import server.login.api.response.UserDto
import server.login.entities.Employee
import server.login.entities.User
import server.login.repositories.AccountRepository
import java.util.stream.Stream

@Service
@Transactional
class AccountServiceImpl(@Autowired private val passwordEncoder: PasswordEncoder,
                         @Autowired private val accountRepository: AccountRepository) : AccountService {

    override fun saveUser(userDto: UserDto): User {

        val user = Employee(
            userDto.username,
            passwordEncoder.encode(userDto.password))

        return accountRepository.save(user)
    }

    override fun getUser(username: String): User {
        return accountRepository.findById(username).get()
    }

    override fun listUsers(): Stream<User> {
        return accountRepository.findAll().stream()
    }

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) throw UsernameNotFoundException("");

        val user = getUser(username)

        return org.springframework.security.core.userdetails.User.builder()
            .username(username)
            .password(user.encodedPassword)
            .roles(username.javaClass.simpleName)
            .build()
    }
}