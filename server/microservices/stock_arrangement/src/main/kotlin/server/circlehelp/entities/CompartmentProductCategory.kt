package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.mapping.IdentifiableTypeClass
import server.circlehelp.entities.base.IdObjectBase

@Entity
class CompartmentProductCategory(
    @OneToOne @JoinColumn(nullable = false) var compartment: Compartment,
    @ManyToOne @JoinColumn(nullable = false) var productCategory: ProductCategory,
    @Id @GeneratedValue override var id: Long? = null
) : IdObjectBase<Long>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompartmentProductCategory

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