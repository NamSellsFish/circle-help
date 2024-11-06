package server.login.value_classes

import org.springframework.security.crypto.password.PasswordEncoder

@JvmInline
value class Password(val value: String) {

    fun encode(passwordEncoder: PasswordEncoder): EncodedPassword {
        val encodedPassword = passwordEncoder.encode(value)
        return EncodedPassword(encodedPassword)
    }
}