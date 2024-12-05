package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import lombok.AccessLevel
import lombok.EqualsAndHashCode
import lombok.With
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.data.domain.Sort
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.auth.User
import server.circlehelp.delegated_classes.Autowirable
import server.circlehelp.repositories.AppraisableRepository
import server.circlehelp.services.TableAuditingService
import java.time.LocalDateTime

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(TableAuditingService::class)
class Appraisable(

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val submitter: Employee,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val topic: Topic,

    val createdDate: LocalDateTime = LocalDateTime.now(),

    @Id @GeneratedValue @EqualsAndHashCode.Include
    val id: Long? = null,
) : Autowirable {

    @Transient
    protected lateinit var applicationContext: ApplicationContext

    @Embedded
    var approvalData: ApprovalData = ApprovalData()
        protected set

    @Throws(IllegalStateException::class)
    fun appraise(appraiser: Admin, approved: Boolean, reason: String = ""): Any {
        if (id == null) {
            throw IllegalStateException(
                "Cannot appraise non-persisted appraisable."
            )
        }

        if (approvalData.approved != null) {
            throw IllegalStateException(
                "Object already ${approvedBooleanToString(this.approvalData.approved)}."
            )
        }

        val result = when(approved) {

            true -> {
                topic.submit()
            }

            false -> {
                topic.reject()
            }
        }

        approvalData = ApprovalData(
            appraiser, approved, reason, LocalDateTime.now()
        )

        val repository = applicationContext.getBean<AppraisableRepository>()

        repository.save(this)

        return result
    }

    @Embeddable
    class ApprovalData(
        @ManyToOne(fetch = FetchType.LAZY)
        val appraiser: Admin? = null,

        val approved: Boolean? = null,

        @Column(nullable = false, length = 255)
        val reason: String = "",

        val approvalDate: LocalDateTime? = null
    )

    companion object {
        fun approvedBooleanToString(approved: Boolean?) : String {
            return when(approved) {
                true -> "approved"
                false -> "rejected"
                null -> "pending"
            }
        }

        val creationDateSort = Sort.by(Sort.Order(
            Sort.Direction.DESC, Appraisable::createdDate.name))

        val approvalDateSort = Sort.by(Sort.Order(
            Sort.Direction.DESC, "${Appraisable::approvalData.name}.${ApprovalData::approvalDate.name}"))

        val defaultSort = approvalDateSort.and(creationDateSort)
    }

    override fun autowireApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Appraisable

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}