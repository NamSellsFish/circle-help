package server.circlehelp.auth

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue(Roles.Employee)
class Employee(username: String,
               encodedPassword: EncodedPassword) : User(username, encodedPassword) {

    override fun getRole() = Roles.Employee

}