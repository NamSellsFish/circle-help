package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.validation.constraints.Size
import java.math.BigDecimal

@Entity
class WorkplaceLocation(
    @Size(min = 1, max = 255)
    @Column(length = 255)
    @Id
    val workplaceName: String,

    @Embedded
    val location: Location,
) {
}