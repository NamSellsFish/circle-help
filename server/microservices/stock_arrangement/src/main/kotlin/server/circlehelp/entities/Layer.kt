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
import server.circlehelp.entities.base.IdObjectBase

@Entity
class Layer(

    @NotNull @Min(1)
    @Column(unique = true, nullable = false)
    var number: Int,

    @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>()  {
}