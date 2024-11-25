package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import lombok.EqualsAndHashCode

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class Layer(

    @NotNull @Min(1)
    @Column(unique = true, nullable = false)
    val number: Int,

    @Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
) {
}