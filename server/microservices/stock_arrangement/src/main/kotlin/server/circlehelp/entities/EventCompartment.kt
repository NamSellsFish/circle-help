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
class EventCompartment(@OneToOne @MapsId("id") val compartment: Compartment
) : IdObjectBase<Long>() {

    @Id @JoinColumn(name = "compartment_id") override val id: Long = compartment.id!!
}