package server.server.login.api.request

import server.login.value_classes.Password

data class UpdateProfileRequest(val email: String?,
                                val username: String?,
                                val password: Password?)