package server.circlehelp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.auth.AccountRepository
import server.circlehelp.auth.User
import server.circlehelp.repositories.readonly.ReadonlyAccountRepository
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Service
@RepeatableReadTransaction(readOnly = true)
class AccountService(
    private val readonlyAccountRepository: ReadonlyAccountRepository
) : UserDetailsService, AuditorAware<User> {


    @Throws(UsernameNotFoundException::class)
    fun getUser(securityContext: SecurityContext) : User? {
        val username = securityContext.authentication.name
        return readonlyAccountRepository.findById(username).getOrNull()
    }

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) throw UsernameNotFoundException(null)

        val user = readonlyAccountRepository.findById(username).getOrNull()
            ?: throw UsernameNotFoundException(username)

        return org.springframework.security.core.userdetails.User.builder()
            .username(username)
            .password(user.encodedPassword.value)
            .roles(user.getRole())
            .build()
    }

    override fun getCurrentAuditor(): Optional<User> {
        return Optional.ofNullable(getUser(SecurityContextHolder.getContext()))
    }
}