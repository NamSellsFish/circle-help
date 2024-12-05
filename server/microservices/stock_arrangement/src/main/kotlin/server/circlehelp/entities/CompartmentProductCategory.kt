package server.circlehelp.entities

import jakarta.persistence.FetchType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.EqualsAndHashCode
import server.circlehelp.services.TableAuditingService

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class CompartmentProductCategory(
    @OneToOne @MapsId("compartmentID") val compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val productCategory: ProductCategory,

) {

    @Id
    @EqualsAndHashCode.Include
    val compartmentID: Long = compartment.id!!
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompartmentProductCategory

        return compartmentID == other.compartmentID
    }

    override fun hashCode(): Int {
        return compartmentID.hashCode()
    }

}