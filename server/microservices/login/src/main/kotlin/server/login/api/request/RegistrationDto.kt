package server.login.api.request

import server.login.entities.Roles
import server.login.value_classes.Password

data class RegistrationDto(val username: String,
                           val password: Password,
                           val role: String) {
}
