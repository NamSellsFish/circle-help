package server.circlehelp.auth

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
abstract class User(@Size(min = 1, max = 20) @NotNull @Column(nullable = false, length = 20)
                    open var username: String,
                    @Id @Email @Column(length = 80) open var email: String,
                    @Column(nullable = false, columnDefinition = "CHAR(68)")
                    open var encodedPassword: EncodedPassword)
{

    abstract fun getRole() : String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return email == other.email
    }

    override fun hashCode(): Int {
        return email.hashCode()
    }
}