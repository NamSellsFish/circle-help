package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne

@Entity
class Row(
    @Id @GeneratedValue var id: Long? = null,
    @ManyToOne var shelf: Shelf,
    @Column(unique=true) var number: Int,
    var compartmentCount: Int
)