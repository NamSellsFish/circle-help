package server.circlehelp.configuration

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import server.circlehelp.api.baseURL
import server.circlehelp.api.inventory
import server.circlehelp.api.management
import server.circlehelp.api.shelf
import server.circlehelp.api.submitOrder
import server.circlehelp.api.submittedOrders
import server.circlehelp.api.test
import server.circlehelp.auth.Roles

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
                authorize(EndpointRequest.toAnyEndpoint().excluding(HealthEndpoint::class.java)
                , hasRole(Roles.Admin))
                authorize("/admin/**", hasRole(Roles.Admin))

                authorize("$baseURL$shelf/*", authenticated)
                authorize("$baseURL$shelf$test/*", authenticated)

                authorize("$baseURL$inventory", authenticated)
                authorize("$baseURL$inventory$submitOrder", hasRole(Roles.Employee))
                authorize("$baseURL$inventory$submittedOrders", hasRole(Roles.Employee))

                authorize("$baseURL/*", authenticated)

                authorize("/kill", permitAll)

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