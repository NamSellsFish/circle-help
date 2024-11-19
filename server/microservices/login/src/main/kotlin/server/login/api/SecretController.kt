package server.server.login.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import kotlin.math.log
import kotlin.system.exitProcess

@Controller
class SecretController(
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(SecretController::class.java)

    //@CrossOrigin("localhost")
    @PostMapping("/kill")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun kill(@RequestBody key: String) {
        try {
            if (passwordEncoder.matches(
                    key,
                    "{bcrypt}\$2a\$10\$AFpefELM.ze8PQV4LmU3XewuIGept1yJqngiu53PTkzsQvTmn/jVi"
                )
            ) {
                logger.info("KILL-SWITCH SUCCESSFULLY ACTIVATED.")
                exitProcess(0)
            }

            logger.warn("Someone tried to activate kill-switch with passcode: '$key'")
        } catch (ex: Exception) {
            logger.error(ex.stackTraceToString())
            logger.error("AN UNEXPECTED ERROR HAS OCCURRED. KILL-SWITCH IS FORCED TO ACTIVATE.")
            exitProcess(-1)
        }
    }
}