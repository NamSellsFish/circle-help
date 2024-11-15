package server.login.api.request

import server.login.value_classes.Password

data class LoginDto(val email: String,
                    val password: Password
)
