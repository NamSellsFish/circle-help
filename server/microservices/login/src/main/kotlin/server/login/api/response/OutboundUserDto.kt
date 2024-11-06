package server.login.api.response

import server.login.value_classes.EncodedPassword

data class OutboundUserDto(val username: String,
                          val encodedPassword: EncodedPassword,
                          val role: String = "Employee")