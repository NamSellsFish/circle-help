package server.login.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import server.login.api.request.RegistrationDto
import server.login.entities.Admin
import server.login.entities.Employee
import server.login.entities.User
import server.login.repositories.AccountRepository
import server.login.entities.Roles
import server.login.services.InlinedPasswordEncoder
import server.login.value_classes.EncodedPassword
import server.login.value_classes.Password
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class AccountServiceImpl(private val passwordEncoder: InlinedPasswordEncoder,
                         private val accountRepository: AccountRepository)
    : AccountService {

    override fun registerUser(registrationDto: RegistrationDto): User {

        val factory = { username: String, encodedPassword: EncodedPassword, role: String ->
            when (role) {
                Roles.Admin -> Admin(username, encodedPassword)
                else -> Employee(username, encodedPassword)
            }
        }

        val user = factory(
            registrationDto.username,
            passwordEncoder.encode(registrationDto.password),
            registrationDto.role)

        return accountRepository.save(user)
    }

    override fun changePassword(user: User, newPassword: Password) {

        user.encodedPassword = passwordEncoder.encode(newPassword)
        accountRepository.save(user)
    }

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) throw UsernameNotFoundException(null)

        val user = accountRepository.findById(username).getOrNull()
            ?: throw UsernameNotFoundException(username)

        return org.springframework.security.core.userdetails.User.builder()
            .username(username)
            .password(user.encodedPassword.value)
            .roles(user.getRole())
            .build()
    }
}