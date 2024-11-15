package server.login.entities

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import server.login.value_classes.EncodedPassword

@Entity
@DiscriminatorValue(Roles.Employee)
class Employee(username: String,
               email: String,
               encodedPassword: EncodedPassword) : User(username, email, encodedPassword) {

    override fun getRole() = Roles.Employee

}