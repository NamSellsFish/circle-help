package server.login.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DuplicateKeyException
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
import server.server.login.api.request.UpdateProfileRequest
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
class AccountServiceImpl(private val passwordEncoder: InlinedPasswordEncoder,
                         private val accountRepository: AccountRepository)
    : AccountService {

    override fun registerUser(registrationDto: RegistrationDto): User {

        val factory = {
            username: String,
            encodedPassword: EncodedPassword,
            email: String,
            role: String ->
            when (role) {
                Roles.Admin -> Admin(username, email,  encodedPassword)
                Roles.Employee -> Employee(username, email, encodedPassword)
                else -> throw IllegalArgumentException("Role '$role' is not supported.")
            }
        }

        val user = factory(
            registrationDto.username,
            passwordEncoder.encode(registrationDto.password),
            registrationDto.email,
            registrationDto.role)

        return accountRepository.save(user)
    }

    override fun updateUser(user: User, updateProfileRequest: UpdateProfileRequest) {

        val (newEmail, newUsername, newPassword) = updateProfileRequest

        if (newEmail != null && newEmail != user.email) {
            if (accountRepository.existsById(newEmail))
                throw DuplicateKeyException("$newEmail already exists.")

            user.email = newEmail
        }

        if (newUsername != null) {
            user.username = newUsername
        }

        if (newPassword != null)
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