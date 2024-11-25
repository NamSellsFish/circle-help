package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode

@Entity
@Table(indexes = [Index(columnList = "product_sku")])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ImageSource(
    @NotNull @NotBlank
    @jakarta.persistence.Id @NotEmpty
    @EqualsAndHashCode.Include
    val url: String,

    @OneToOne @JoinColumn(nullable = false)
    val product: Product
) {
}