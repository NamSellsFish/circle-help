package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import org.jetbrains.annotations.NotNull
import server.circlehelp.api.response.CompartmentPosition

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["layer_id", "number"])])
class Compartment(
    @ManyToOne var layer: Layer,

    @NotNull @Min(1)
    @Column(nullable = false)
    var number: Int,

    @NotNull @Min(1)
    @Column(nullable = false, unique = true)
    var compartmentNoFromUserPerspective: Int,

    @Id @GeneratedValue var id: Long? = null
) {


    fun getLocation() = CompartmentPosition(layer.shelf.number, layer.number, number)

    fun equals(shelfNumber: Int, layerNumber: Int, compartmentNumber: Int): Boolean {
        return number == compartmentNumber
                && layer.number == layerNumber
                && layer.shelf.number == shelfNumber
    }


}