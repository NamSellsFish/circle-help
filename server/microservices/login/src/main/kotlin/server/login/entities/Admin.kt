package server.login.entities

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("Admin")
class Admin(username: String,
               encodedPassword: String) : User(username, encodedPassword) {

}