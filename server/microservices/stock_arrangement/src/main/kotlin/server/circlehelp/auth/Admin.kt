package server.circlehelp.auth

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(Roles.Admin)
class Admin(username: String,
            email: String,
            encodedPassword: EncodedPassword) : User(username, email, encodedPassword) {
    override fun getRole() = Roles.Admin


}