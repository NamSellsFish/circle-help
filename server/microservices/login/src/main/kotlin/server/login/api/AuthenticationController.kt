package server.login.api

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import server.login.api.URIs.baseURI
import server.login.api.response.InboundUserRequest
import server.login.services.AccountService
import server.login.api.request.RegistrationDto
import server.login.api.response.OutboundUserDto
import server.login.repositories.readonly.ReadonlyAccountRepository
import server.login.value_classes.FuncChain
import server.login.value_classes.Password
import java.util.Base64
import kotlin.jvm.optionals.getOrNull

@Controller
@RequestMapping(baseURI)
class AuthenticationController(private val authenticationManager: AuthenticationManager,
                               private val accountService: AccountService,
                               private val securityContextRepository: SecurityContextRepository,
                               private val readonlyAccountRepository: ReadonlyAccountRepository,
                               mapperBuilder: Jackson2ObjectMapperBuilder )
{

    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    data class OutboundUserResponse(val user: OutboundUserDto)
    data class PasswordRequest(val password: Password)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun registerUser(@RequestBody inboundUserRequest: InboundUserRequest<RegistrationDto>): OutboundUserResponse {

        val user = accountService.registerUser(inboundUserRequest.user)
        return OutboundUserResponse(
            OutboundUserDto(
                user.username,
                user.encodedPassword,
                user.getRole()))
    }

    @PatchMapping("/changePassword")
    fun changePassword(@RequestBody changePasswordRequest: PasswordRequest,
                       request: HttpServletRequest,
                       response: HttpServletResponse,
                       @CurrentSecurityContext securityContext: SecurityContext) : ResponseEntity<String> {

        val username = securityContext.authentication.name
        val user = readonlyAccountRepository.findById(username).getOrNull()
            ?: return ResponseEntity
                .unprocessableEntity()
                .body("User '$username' not found.")

        accountService.changePassword(user, changePasswordRequest.password)

        return ResponseEntity
            .ok()
            .body("$username changed password.")
    }

    @PostMapping("/login")
    fun login(@RequestHeader(HttpHeaders.AUTHORIZATION) authInfo: String,
              request: HttpServletRequest,
              response: HttpServletResponse,
              @CurrentSecurityContext securityContext: SecurityContext) : ResponseEntity<String> {

        securityContextRepository.saveContext(securityContext, request, response)

        val username = securityContext.authentication.name
        val user = readonlyAccountRepository.findById(securityContext.authentication.name).getOrNull()
            ?: return ResponseEntity
                .unprocessableEntity()
                .body("User '$username' not found.")

        val userDto = OutboundUserDto(
            user.email,
            user.encodedPassword,
            user.username,
            user.getRole()
        )

        return ResponseEntity.ok(objectMapper.writeValueAsString(OutboundUserResponse(userDto)))
    }

    private fun decodeBasicAuth(authInfo: String): String {
        val encodedStr = authInfo.substring("Basic ".length)
        val usernameAndPasswordString = String(Base64.getDecoder().decode(encodedStr))

        val colonIndex = usernameAndPasswordString.indexOf(':')

        val username = usernameAndPasswordString.substring(0..<colonIndex)
        val password = usernameAndPasswordString.substring(colonIndex + 1)

        return """Logged in as: $username"""
    }
}