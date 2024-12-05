package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import lombok.EqualsAndHashCode
import server.circlehelp.services.TableAuditingService

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class Layer(

    @NotNull @Min(1)
    @Column(unique = true, nullable = false)
    val number: Int,

    @Id @GeneratedValue
    @EqualsAndHashCode.Include
    val id: Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Layer

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}