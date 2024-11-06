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

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["shelf_id", "number"])])
class Layer(
    @ManyToOne var shelf: Shelf,

    @NotNull @Min(1)
    @Column(nullable = false)
    var number: Int,

    @Id @GeneratedValue var id: Long? = null
) {
}