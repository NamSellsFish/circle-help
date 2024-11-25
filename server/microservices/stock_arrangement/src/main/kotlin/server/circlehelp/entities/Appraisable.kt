package server.circlehelp.entities

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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
import server.circlehelp.auth.Admin
import server.circlehelp.auth.Employee
import server.circlehelp.auth.User
import server.circlehelp.delegated_classes.Autowirable
import server.circlehelp.repositories.AppraisableRepository
import java.time.LocalDateTime

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class Appraisable(

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val submitter: Employee,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val topic: Topic,

    @Id @GeneratedValue @EqualsAndHashCode.Include
    val id: Long? = null,
) : Autowirable {

    val createdDate: LocalDateTime = LocalDateTime.now()

    @Transient
    protected lateinit var applicationContext: ApplicationContext

    @Embedded
    var approvalData: ApprovalData = ApprovalData()
        protected set

    @Throws(IllegalStateException::class)
    fun appraise(appraiser: Admin, approved: Boolean, reason: String = ""): Any {
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
    }

    override fun autowireApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}