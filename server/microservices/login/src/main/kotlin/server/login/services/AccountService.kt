package server.login.services

import org.springframework.security.core.userdetails.UserDetailsService
import server.login.api.request.RegistrationDto
import server.login.entities.User
import server.login.value_classes.Password

interface AccountService : UserDetailsService {

    fun registerUser(registrationDto: RegistrationDto): User

    fun changePassword(user: User, newPassword: Password)
}