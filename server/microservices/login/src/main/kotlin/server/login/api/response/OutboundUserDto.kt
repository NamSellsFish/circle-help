package server.login.api.response

import server.login.entities.User
import server.login.value_classes.EncodedPassword

data class OutboundUserDto(val email: String,
                          val encodedPassword: EncodedPassword,
                           val username: String,
                          val role: String = "Employee") {
    constructor(user: User) : this(user.email, user.encodedPassword, user.username, user.getRole())
}