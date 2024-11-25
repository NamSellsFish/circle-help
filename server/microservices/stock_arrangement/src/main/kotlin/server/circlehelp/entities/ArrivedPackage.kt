package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import java.time.LocalDateTime

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ArrivedPackage(
    @NotNull @NotBlank @Size(min = 1, max = 20)
    @Column(nullable = false)
    val supplier: String,

    @NotNull @PastOrPresent
    @Column(nullable = false)
    val dateTime: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue @EqualsAndHashCode.Include
    val id: Long? = null
) {
}