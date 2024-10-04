package server.login.entities

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("Employee")
class Employee(username: String,
               encodedPassword: String) : User(username, encodedPassword) {

}