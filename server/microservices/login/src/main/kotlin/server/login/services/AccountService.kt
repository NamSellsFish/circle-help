package server.login.services

import org.springframework.dao.DuplicateKeyException
import org.springframework.security.core.userdetails.UserDetailsService
import server.login.api.request.RegistrationDto
import server.login.entities.User
import server.login.value_classes.Password
import server.server.login.api.request.UpdateProfileRequest

interface AccountService : UserDetailsService {

    @Throws(DuplicateKeyException::class)
    fun registerUser(registrationDto: RegistrationDto): User

    @Throws(DuplicateKeyException::class)
    fun updateUser(user: User, updateProfileRequest: UpdateProfileRequest)
}