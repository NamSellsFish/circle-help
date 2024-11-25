package server.login.api

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import server.login.api.URIs.baseURI
import server.login.api.response.InboundUserRequest
import server.login.services.AccountService
import server.login.api.request.RegistrationDto
import server.login.api.response.OutboundUserDto
import server.login.entities.User
import server.login.repositories.readonly.ReadonlyAccountRepository
import server.server.login.api.request.UpdateProfileRequest
import java.util.Base64
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping(baseURI)
@Transactional
class AuthenticationController(private val authenticationManager: AuthenticationManager,
                               private val accountService: AccountService,
                               private val securityContextRepository: SecurityContextRepository,
                               private val readonlyAccountRepository: ReadonlyAccountRepository,
                               private val sessionRegistry: SessionRegistry,
                               mapperBuilder: Jackson2ObjectMapperBuilder )
{

    private val objectMapper = mapperBuilder.build<ObjectMapper>()
    data class OutboundUserResponse(val user: OutboundUserDto)

    @Throws(UsernameNotFoundException::class)
    private fun getUser(securityContext: SecurityContext) : User {
        val username = securityContext.authentication.name
        return readonlyAccountRepository.findById(username).getOrNull()
            ?: throw UsernameNotFoundException("User '$username' not found.")
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody inboundUserRequest: InboundUserRequest<RegistrationDto>): OutboundUserResponse {

        val user = accountService.registerUser(inboundUserRequest.user)
        return OutboundUserResponse(
            OutboundUserDto(user))
    }

    @GetMapping("/getProfile")
    @ResponseStatus(HttpStatus.OK)
    fun getProfile(@CurrentSecurityContext securityContext: SecurityContext) : OutboundUserResponse {
        val user = getUser(securityContext)
        return OutboundUserResponse(OutboundUserDto(user))
    }

    @PatchMapping("/updateProfile")
    @Throws(DuplicateKeyException::class)
    fun updateProfile(@RequestBody updateProfileRequest: UpdateProfileRequest,
                      request: HttpServletRequest,
                      response: HttpServletResponse,
                      @CurrentSecurityContext securityContext: SecurityContext) : OutboundUserResponse {

        val username = securityContext.authentication.name
        val user = getUser(securityContext)

        accountService.updateUser(user, updateProfileRequest)

        return OutboundUserResponse(OutboundUserDto(user))
    }

    @PostMapping("/login")
    fun login(@RequestHeader(HttpHeaders.AUTHORIZATION) authInfo: String,
              request: HttpServletRequest,
              response: HttpServletResponse,
              @CurrentSecurityContext securityContext: SecurityContext) : OutboundUserResponse {


        val authentication = decodeBasicAuth(authInfo)

        SecurityContextLogoutHandler().logout(request, response, securityContext.authentication)



        val newSecurityContext = SecurityContextHolder.getContext()

        newSecurityContext.authentication = authenticationManager.authenticate(authentication)

        securityContextRepository.saveContext(newSecurityContext, request, response)

        val username = authentication.name
        val user = readonlyAccountRepository.findById(username).get()

        return OutboundUserResponse(OutboundUserDto(user))
    }

    private fun decodeBasicAuth(authInfo: String): UsernamePasswordAuthenticationToken {
        val encodedStr = authInfo.substring("Basic ".length)
        val usernameAndPasswordString = String(Base64.getDecoder().decode(encodedStr))

        val colonIndex = usernameAndPasswordString.indexOf(':')

        val username = usernameAndPasswordString.substring(0..<colonIndex)
        val password = usernameAndPasswordString.substring(colonIndex + 1)

        return UsernamePasswordAuthenticationToken(username, password)
    }
}