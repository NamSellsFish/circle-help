package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["shelf", "number"])])
class Layer(
    @ManyToOne var shelf: Shelf,
    @Column var number: Int,
    var compartmentCount: Int = 5,
    @Id @GeneratedValue var id: Long? = null
) {
}