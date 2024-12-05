package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.EqualsAndHashCode
import org.springframework.data.domain.Sort
import server.circlehelp.services.TableAuditingService

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class EventCompartment(@OneToOne @MapsId("compartmentID") val compartment: Compartment
) {

    @Id
    @EqualsAndHashCode.Include
    val compartmentID: Long = compartment.id!!

    companion object {
        val layerSort = Sort.by("${EventCompartment::compartment.name}.${Compartment::layer.name}.${Layer::number.name}")
        val numberSort = Sort.by("${EventCompartment::compartment.name}.${Compartment::number.name}")

        val layerAscNumberAscSort = layerSort.ascending().and(numberSort.ascending())
        val numberAscLayerAscSort = numberSort.ascending().and(layerSort.ascending())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventCompartment

        return compartmentID == other.compartmentID
    }

    override fun hashCode(): Int {
        return compartmentID.hashCode()
    }
}