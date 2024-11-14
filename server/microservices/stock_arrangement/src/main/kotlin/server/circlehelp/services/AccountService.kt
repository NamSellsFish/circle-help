package server.circlehelp.services

import jakarta.transaction.Transactional
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction
import server.circlehelp.auth.AccountRepository
import kotlin.jvm.optionals.getOrNull

@Service
@RepeatableReadTransaction
class AccountService(private val accountRepository: AccountRepository) : UserDetailsService{


    override fun loadUserByUsername(username: String?): UserDetails {
        if (username == null) throw UsernameNotFoundException(null)

        val user = accountRepository.findById(username).getOrNull()
            ?: throw UsernameNotFoundException(username)

        return org.springframework.security.core.userdetails.User.builder()
            .username(username)
            .password(user.encodedPassword.value)
            .roles(user.getRole())
            .build()
    }
}