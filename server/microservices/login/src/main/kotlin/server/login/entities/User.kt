package server.login.entities

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import org.springframework.security.core.userdetails.UserDetails
import server.login.value_classes.EncodedPassword

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
abstract class User(@Id open var username: String,
                    @Column(nullable = false) open var encodedPassword: EncodedPassword)
{

    abstract fun getRole() : String


}