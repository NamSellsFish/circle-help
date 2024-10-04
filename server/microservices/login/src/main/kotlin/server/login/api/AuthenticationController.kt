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
import server.login.api.response.UserDto
import server.login.entities.User
import server.login.services.AccountService

@Controller
@RequestMapping("/api/auth")
class AuthenticationController(@Autowired private val authenticationManager: AuthenticationManager,
                               @Autowired private val accountService: AccountService) {

    data class UserResponse(val user: UserDto)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun registerUser(@RequestBody userResponse: UserResponse): User {

        return accountService.saveUser(userResponse.user)
    }

    @PostMapping("/login", consumes = [ MediaType.ALL_VALUE ])
    fun loginUser(@RequestBody userResponse: UserResponse): ResponseEntity<UserResponse> {

        val username = userResponse.user.username
        val password = userResponse.user.password

        val authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                username, password)

        val authenticationResponse =
            authenticationManager.authenticate(authenticationRequest)

        val user = accountService.getUser(username)

        return ResponseEntity(UserResponse(UserDto(user.username, user.encodedPassword)), HttpStatus.ACCEPTED)
    }
}