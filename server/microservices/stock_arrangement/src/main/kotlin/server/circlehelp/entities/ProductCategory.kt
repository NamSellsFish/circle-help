package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.Id

@Entity
class ProductCategory(
    @NotNull @NotBlank
    @Size(min = 2, max = 2)
    @Column(columnDefinition = "CHAR(2)")
    @jakarta.persistence.Id var id: String,

    @NotNull @NotBlank
    @Size(min = 1, max = 30)
    @Column(length = 30, nullable = false, unique = true)
    var name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductCategory

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}