package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
@Entity
class EventCompartment(@OneToOne val compartment: Compartment,
                       @Id @GeneratedValue var id: Long? = null) {
}