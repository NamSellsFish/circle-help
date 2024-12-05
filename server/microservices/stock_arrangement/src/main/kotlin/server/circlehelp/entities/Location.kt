package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
data class Location(
    @Column(columnDefinition = "DECIMAL(38, 7)", nullable = false)
    val latitude: BigDecimal,
    @Column(columnDefinition = "DECIMAL(38, 7)", nullable = false)
    val longitude: BigDecimal
)
