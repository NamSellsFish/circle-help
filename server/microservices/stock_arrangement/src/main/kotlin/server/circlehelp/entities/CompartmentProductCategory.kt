package server.circlehelp.entities

import jakarta.persistence.FetchType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import org.hibernate.mapping.IdentifiableTypeClass
import server.circlehelp.entities.base.IdObjectBase

@Entity
class CompartmentProductCategory(
    @OneToOne @MapsId("id") var compartment: Compartment,
    @ManyToOne(optional = false, fetch = FetchType.LAZY) var productCategory: ProductCategory,

) : IdObjectBase<Long>() {

    @Id @JoinColumn(name = "compartment_id") override val id: Long = compartment.id!!

}