package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
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
import server.circlehelp.services.TableAuditingService
import server.circlehelp.value_classes.UrlValue

@Entity
@Table(indexes = [Index(columnList = "product_sku")])
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class ImageSource(
    @NotNull @NotBlank
    @jakarta.persistence.Id @NotEmpty
    @EqualsAndHashCode.Include
    val url: UrlValue,

    @OneToOne @JoinColumn(nullable = false)
    val product: Product
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageSource

        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}