package server.login.api.response

data class UserDto(val username: String,
                   val password: String,
                   val role: String = "Employee")
