package server.circlehelp.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
class SecurityFilterConfig {

    @Bean
    fun baseSecurityFilterChain(http: HttpSecurity,
                                rememberMeServices: RememberMeServices,
                                securityContextRepository: SecurityContextRepository
    ) : SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(anyRequest, authenticated)
            }
            csrf {
                disable()
                csrfTokenRepository = CookieCsrfTokenRepository()
            }
            securityContext {
                this.securityContextRepository = securityContextRepository
            }
            rememberMe {
                this.rememberMeServices = rememberMeServices
            }
            httpBasic {  }
        }

        return http.build()
    }
}