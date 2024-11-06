package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne

@Entity
class FrontCompartment(@OneToOne val compartment: Compartment,
                       @Id @GeneratedValue var id: Long? = null) {
}