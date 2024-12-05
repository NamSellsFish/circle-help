package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.persistence.FetchType
import jakarta.persistence.Version
import lombok.EqualsAndHashCode
import org.springframework.data.domain.Sort

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//@EntityListeners(TableAuditingService::class)
//@With
class ProductOnCompartment(
    @OneToOne @MapsId("compartmentID") val compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val packageProduct: PackageProduct,

    @NotNull @Min(0)
    @Column(nullable = false)
    var status: Int = 1,

    @Version
    val version: Int = 0
) {

    @Id val compartmentID: Long = compartment.id!!

    fun statusUpdateAllowed() : Boolean {
        return status != 2
    }

    fun with(compartment: Compartment? = null,
             packageProduct: PackageProduct? = null,
             status: Int? = null,
             version: Int? = null) : ProductOnCompartment {

        /*
        if (compartment == null &&
            packageProduct == null &&
            status == null &&
            version == null)
            return this
         */

        return ProductOnCompartment(
            compartment ?: this.compartment,
            packageProduct ?: this.packageProduct,
            status ?: this.status,
            version ?: this.version,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductOnCompartment

        return compartmentID == other.compartmentID
    }

    override fun hashCode(): Int {
        return compartmentID.hashCode()
    }

    override fun toString(): String {
        return "ProductOnCompartment(packageProduct=$packageProduct, status=$status, version=$version, compartment=$compartment)"
    }


    companion object {
        val layerSort = Sort.by("${ProductOnCompartment::compartment.name}.${Compartment::layer.name}.${Layer::number.name}")
        val numberSort = Sort.by("${ProductOnCompartment::compartment.name}.${Compartment::number.name}")

        val layerAscNumberAscSort = layerSort.ascending().and(numberSort.ascending())
        val numberAscLayerAscSort = numberSort.ascending().and(layerSort.ascending())
    }
}