package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import org.jetbrains.annotations.NotNull
import server.circlehelp.api.response.CompartmentPosition

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["layer_id", "number"])])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class Compartment(
    @ManyToOne(optional = false, fetch = FetchType.LAZY) val layer: Layer,

    @NotNull @Min(0)
    @Column(nullable = false)
    val number: Int,

    @NotNull @Size(min = 3, max = 4)
    @Column(nullable = false, length = 4)
    var compartmentNoFromUserPerspective: String,

    @Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
) {

    fun getLocation() = CompartmentPosition(layer.number, number)

    fun equals(shelfNumber: Int, layerNumber: Int, compartmentNumber: Int): Boolean {
        return number == compartmentNumber
                && layer.number == layerNumber
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Compartment

        if (id == null)
            throw NullPointerException("this.id")

        if (other.id == null)
            throw NullPointerException("other.id")

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}