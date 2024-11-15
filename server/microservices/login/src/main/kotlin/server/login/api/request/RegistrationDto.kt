package server.login.api.request

import server.login.entities.Roles
import server.login.value_classes.Password

data class RegistrationDto(val email: String,
                           val username: String,
                           val password: Password,
                           val role: String) {
}
