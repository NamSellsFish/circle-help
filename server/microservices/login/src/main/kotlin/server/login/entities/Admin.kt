package server.login.entities

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import server.login.value_classes.EncodedPassword

@Entity
@DiscriminatorValue(Roles.Admin)
class Admin(username: String,
               encodedPassword: EncodedPassword) : User(username, encodedPassword) {
    override fun getRole() = Roles.Admin


}