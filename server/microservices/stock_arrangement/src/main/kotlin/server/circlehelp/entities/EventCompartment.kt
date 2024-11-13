package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import server.circlehelp.entities.base.IdObjectBase

@Entity
class EventCompartment(@OneToOne @JoinColumn(nullable = false) val compartment: Compartment,
                       @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {
}