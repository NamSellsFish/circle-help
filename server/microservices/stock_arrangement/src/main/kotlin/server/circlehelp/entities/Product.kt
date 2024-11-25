package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import java.math.BigDecimal

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
}