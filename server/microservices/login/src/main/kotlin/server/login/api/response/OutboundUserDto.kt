package server.server.login.api.response

data class OutboundUserDto(val username: String,
                          val encodedPassword: String,
                          val role: String = "Employee")