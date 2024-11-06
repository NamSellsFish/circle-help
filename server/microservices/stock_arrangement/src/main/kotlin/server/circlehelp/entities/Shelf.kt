package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@Entity
class Shelf(
    @NotNull @Min(0)
    @Column(unique=true, nullable = false)
    var number: Int,

    @Id @GeneratedValue var id: Long? = null
)