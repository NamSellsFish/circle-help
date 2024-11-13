package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
import server.circlehelp.entities.base.IdObjectBase
import java.time.LocalDateTime

@Entity
class ArrivedPackage (
    @NotNull @PastOrPresent
    @Column(nullable = false)
    var date: LocalDateTime,

    @NotNull @Size(min = 1, max = 20) var supplier: String,
    @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrivedPackage

        if (id == null)
            throw NullPointerException("this.id")

        if (other.id == null)
            throw NullPointerException("other.id")

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}