package server.circlehelp.configuration

import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.RememberMeAuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter
import org.springframework.security.web.context.DelegatingSecurityContextRepository
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import javax.sql.DataSource

/**
 * Dao + Remember me + Sessions
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun httpExchangeRepository(): HttpExchangeRepository {
        return InMemoryHttpExchangeRepository()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        /* Note: Allowing everything like this is not the correct way, so never do this in a practical environment. */
        val configuration = CorsConfiguration()
        configuration.setAllowedOriginPatterns(listOf("*"))
        configuration.allowedMethods =
            listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.exposedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder() : PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun jdbcOperations(dataSource: DataSource): JdbcOperations {
        return JdbcTemplate(dataSource)
    }

    @Bean
    fun daoAuthenticationProvider(userDetailsService: UserDetailsService,
                                  passwordEncoder: PasswordEncoder
    ) : DaoAuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider(passwordEncoder)
        authenticationProvider.setUserDetailsService(userDetailsService)
        return authenticationProvider
    }

    val key = "2414d72f-021f-4729-ac3f-e7f32fa2cab5"

    @Bean
    fun rememberMeAuthenticationProvider(): RememberMeAuthenticationProvider {
        return RememberMeAuthenticationProvider(key)
    }

    @Bean
    fun authenticationManager(
        daoAuthenticationProvider: DaoAuthenticationProvider,
        rememberMeAuthenticationProvider: RememberMeAuthenticationProvider
    ): AuthenticationManager {

        return ProviderManager(
            daoAuthenticationProvider,
            rememberMeAuthenticationProvider
        )
    }

    @Bean
    fun persistentTokenRepository(dataSource: DataSource) : PersistentTokenRepository {
        val repo = JdbcTokenRepositoryImpl()
        repo.setDataSource(dataSource)
        return repo
    }

    @Bean
    fun rememberMeServices(userDetailsService: UserDetailsService,
                           persistentTokenRepository: PersistentTokenRepository
    ): RememberMeServices {
        return PersistentTokenBasedRememberMeServices(
            key, userDetailsService, persistentTokenRepository
        )
    }

    @Bean
    fun securityContextRepository() : SecurityContextRepository = DelegatingSecurityContextRepository(
        RequestAttributeSecurityContextRepository(),
        HttpSessionSecurityContextRepository()
    )

    @Bean
    fun rememberMeFilter(
        authenticationManager: AuthenticationManager,
        rememberMeServices: RememberMeServices
    ): RememberMeAuthenticationFilter {
        return RememberMeAuthenticationFilter(authenticationManager, rememberMeServices)
    }

    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }
}