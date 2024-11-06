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
import java.time.LocalDateTime

@Entity
class ArrivedPackage(
    @NotNull @PastOrPresent
    @Column(nullable = false)
    var date: LocalDateTime,

    @NotNull @Size(min = 1, max = 20) var supplier: String,
    @Id @GeneratedValue var id: Long? = null
)