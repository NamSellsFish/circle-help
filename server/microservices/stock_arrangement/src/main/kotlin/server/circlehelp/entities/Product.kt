package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import server.circlehelp.services.TableAuditingService
import java.math.BigDecimal

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class Product(
    @NotNull @NotBlank
    @Size(min = 6, max = 6)
    @Id @Column(columnDefinition = "CHAR(6)")
    @EqualsAndHashCode.Include
    val sku: String,

    @NotNull @NotBlank
    @Size(min = 1, max = 30)
    @Column(length = 30, nullable = false)
    val name: String,

    @NotNull
    @Min(0)
    @Column(nullable = false)
    val price: BigDecimal
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        return sku == other.sku
    }

    override fun hashCode(): Int {
        return sku.hashCode()
    }

}