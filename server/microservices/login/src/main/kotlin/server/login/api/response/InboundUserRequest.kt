package server.login.api.response

import server.login.api.request.RegistrationDto

data class InboundUserRequest<T>(val user: T)