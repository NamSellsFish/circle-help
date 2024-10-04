package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.jetbrains.annotations.NotNull
import server.circlehelp.api.response.CompartmentLocation

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["layer", "number"])])
class Compartment(
    @ManyToOne var layer: Layer,
    var number: Int,
    @Id @GeneratedValue @NotNull var id: Long? = null
) {


    fun getLocation() = CompartmentLocation(layer.shelf.number, layer.number, number)

    fun equals(shelfNumber: Int, layerNumber: Int, compartmentNumber: Int): Boolean {
        return number == compartmentNumber
                && layer.number == layerNumber
                && layer.shelf.number == shelfNumber
    }


}