package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import org.springframework.data.annotation.Id

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ProductCategory(
    @NotNull @NotBlank
    @Size(min = 2, max = 2)
    @Column(columnDefinition = "CHAR(2)")
    @jakarta.persistence.Id
    @EqualsAndHashCode.Include
    val id: String,

    @NotNull @NotBlank
    @Size(min = 1, max = 30)
    @Column(length = 30, nullable = false, unique = true)
    val name: String
) {
}