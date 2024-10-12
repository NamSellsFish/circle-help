package server.login.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import server.login.api.response.InboundUserDto
import server.login.entities.User
import server.login.services.AccountService
import server.server.login.api.response.OutboundUserDto

@Controller
@RequestMapping("/api/auth")
class AuthenticationController(@Autowired private val authenticationManager: AuthenticationManager,
                               @Autowired private val accountService: AccountService) {

    data class InboundUserResponse(val user: InboundUserDto)
    data class OutboundUserResponse(val user: OutboundUserDto)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun registerUser(@RequestBody inboundUserResponse: InboundUserResponse): OutboundUserResponse {

        val user = accountService.saveUser(inboundUserResponse.user)
        return OutboundUserResponse(OutboundUserDto(user.username, user.encodedPassword, user.javaClass.simpleName))
    }

    @PostMapping("/login", consumes = [ MediaType.ALL_VALUE ])
    fun loginUser(@RequestBody inboundUserResponse: InboundUserResponse): ResponseEntity<OutboundUserResponse> {

        val username = inboundUserResponse.user.username
        val password = inboundUserResponse.user.password

        val authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                username, password)

        val authenticationResponse =
            authenticationManager.authenticate(authenticationRequest)

        val user = accountService.getUser(username)

        return ResponseEntity(OutboundUserResponse(OutboundUserDto(user.username, user.encodedPassword, user.javaClass.simpleName)), HttpStatus.ACCEPTED)
    }
}