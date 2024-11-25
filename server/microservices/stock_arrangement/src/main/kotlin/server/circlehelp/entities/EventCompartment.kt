package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.EqualsAndHashCode

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class EventCompartment(@OneToOne @MapsId("compartmentID") val compartment: Compartment
) {

    @Id
    @EqualsAndHashCode.Include
    val compartmentID: Long = compartment.id!!
}