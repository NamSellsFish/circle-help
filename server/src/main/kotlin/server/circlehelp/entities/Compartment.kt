package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne

@Entity
class Compartment(
    @Id @GeneratedValue var id: Long? = null,
    @ManyToOne var row: Row,
    @Column(unique=true) var number: Int
)