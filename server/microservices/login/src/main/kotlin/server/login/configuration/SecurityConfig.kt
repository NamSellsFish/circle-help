package server.login.configuration

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.RememberMeAuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.RememberMeServices
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter
import org.springframework.security.web.context.DelegatingSecurityContextRepository
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.session.HttpSessionEventPublisher
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import server.login.api.URIs.baseURI
import server.login.api.URIs.oauthURI
import server.login.entities.Roles
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import javax.sql.DataSource
import kotlin.io.path.Path


@Configuration
@EnableWebSecurity
class SecurityConfig(private val env: Environment) {

    val key = "5b641744-b9cf-4f96-98ea-7907850302b1"

    /*
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize("/api/auth/login", permitAll)
                authorize("/api/auth/register", permitAll)
                authorize(anyRequest, authenticated)
            }
            httpBasic {  }
        }

        return http.build()
    }

     */

    @get:Bean
    val securityContextRepository : SecurityContextRepository = DelegatingSecurityContextRepository(
        RequestAttributeSecurityContextRepository(),
        HttpSessionSecurityContextRepository()
    )

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

    @get:Bean
    val passwordEncoder: PasswordEncoder by lazy {
        PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    @Order(1)
    @Throws(Exception::class)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
            .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0
        http { // Redirect to the login page when not authenticated from the
            // authorization endpoint
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.IF_REQUIRED
                sessionConcurrency {
                    maximumSessions = 1
                }
                sessionFixation {  }
            }
            exceptionHandling {
                defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("$oauthURI/login"),
                    MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            } // Accept access tokens for User Info and/or Client Registration
            oauth2ResourceServer {
                jwt { Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> { } }
            }
        }
        return http.build()
    }

    @Bean
    @Order(2)
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity,
                                   rememberMeServices: RememberMeServices,
                                   securityContextRepository: SecurityContextRepository): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("$baseURI/login", permitAll)
                authorize("$baseURI/register", hasRole(Roles.Admin))
                authorize("/kill", permitAll)
                authorize(anyRequest, authenticated)
            }
            csrf {
                ignoringRequestMatchers("$baseURI/*", "/kill")
                csrfTokenRepository = CookieCsrfTokenRepository()
            }
            securityContext {
                this.securityContextRepository = securityContextRepository
            }
            rememberMe {
                this.rememberMeServices = rememberMeServices
            }
            logout {
                logoutUrl = "$baseURI/logout"
                permitAll()
            }
            httpBasic {  }
        }
        return http.build()
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
    fun rememberMeFilter(
        authenticationManager: AuthenticationManager,
        rememberMeServices: RememberMeServices
    ): RememberMeAuthenticationFilter {
        return RememberMeAuthenticationFilter(authenticationManager, rememberMeServices)
    }

    @Bean
    fun rememberMeServices(userDetailsService: UserDetailsService,
                           persistentTokenRepository: PersistentTokenRepository): RememberMeServices {
        return PersistentTokenBasedRememberMeServices(
            key, userDetailsService, persistentTokenRepository
        )
    }

    @Bean
    fun oauth2AuthorizationService(jdbcOperations: JdbcOperations,
                                   registeredClientRepository: RegisteredClientRepository)
    : OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository)
    }

    @Bean
    fun oauth2AuthorizationConsentService(jdbcOperations: JdbcOperations,
                                          registeredClientRepository: RegisteredClientRepository)
    : OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository)
    }

    @Bean
    fun persistentTokenRepository(dataSource: DataSource) : PersistentTokenRepository {
        val repo = JdbcTokenRepositoryImpl()
        repo.setDataSource(dataSource)
        return repo
    }

    @Bean
    fun rememberMeAuthenticationProvider(): RememberMeAuthenticationProvider {
        return RememberMeAuthenticationProvider(key)
    }

    @Bean
    fun daoAuthenticationProvider(userDetailsService: UserDetailsService,
                                  passwordEncoder: PasswordEncoder): DaoAuthenticationProvider {

        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder)
        authenticationProvider.isHideUserNotFoundExceptions = true
        return authenticationProvider
    }

    @Bean
    fun registeredClientRepository(jdbcOperations: JdbcOperations): RegisteredClientRepository {
        return JdbcRegisteredClientRepository(jdbcOperations)
    }

    @Bean
    fun sessionRegistry(): SessionRegistry {
        return SessionRegistryImpl()
    }

    @Bean
    fun httpSessionEventPublisher(): HttpSessionEventPublisher {
        return HttpSessionEventPublisher()
    }

    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {

        val publicKey = getPublicKey("keys/public.der") as RSAPublicKey
        val privateKey = getPrivateKey("keys/private.der") as RSAPrivateKey
        val rsaKey: RSAKey = RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(key)
            .build()
        val jwkSet = JWKSet(rsaKey)
        return ImmutableJWKSet(jwkSet)
    }

    @Throws(java.lang.Exception::class)
    fun getPrivateKey(filename: String): PrivateKey {
        val keyBytes = Files.readAllBytes(Path(filename))
        val spec = PKCS8EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(spec)
    }

    @Throws(java.lang.Exception::class)
    fun getPublicKey(filename: String): PublicKey {
        val keyBytes = Files.readAllBytes(Path(filename))
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec)
    }

    private fun generateRsaKey(): KeyPair {
        val keyPair: KeyPair = try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
        return keyPair
    }

    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().build()
    }

    @Bean
    fun jdbcOperations(dataSource: DataSource): JdbcOperations {
        return JdbcTemplate(dataSource)
    }
}