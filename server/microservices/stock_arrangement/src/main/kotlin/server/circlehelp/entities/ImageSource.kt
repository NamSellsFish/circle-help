package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Entity
@Table(indexes = [Index(columnList = "product_sku")])
class ImageSource(
    @NotNull @NotBlank
    @jakarta.persistence.Id @NotEmpty
    var url: String,

    @OneToOne
    var product: Product
) {
}