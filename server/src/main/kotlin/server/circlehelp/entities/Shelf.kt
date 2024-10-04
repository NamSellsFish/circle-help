package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column

@Entity
class Shelf(
    @Id @GeneratedValue var id: Long? = null,
    @Column(unique=true) var number: Int
)