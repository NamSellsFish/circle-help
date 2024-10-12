package server.login.api.response

data class InboundUserDto(val username: String,
                          val password: String,
                          val role: String = "Employee")
