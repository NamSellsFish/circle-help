package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import lombok.EqualsAndHashCode
import org.springframework.data.domain.Sort
import server.circlehelp.services.TableAuditingService
import java.time.LocalDateTime

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class ArrivedPackage(
    @NotNull @NotBlank @Size(min = 1, max = 20)
    @Column(nullable = false)
    val supplier: String,

    @NotNull @PastOrPresent
    @Column(nullable = false)
    val dateTime: LocalDateTime = LocalDateTime.now(),

    @Id @EqualsAndHashCode.Include
    var id: Long? = null
) {

    companion object {

        val defaultSort = Sort.by(Sort.Order(Sort.Direction.DESC, ArrivedPackage::dateTime.name),
            Sort.Order(Sort.Direction.DESC, ArrivedPackage::id.name))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrivedPackage

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}