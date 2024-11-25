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
import lombok.With
import org.springframework.context.ApplicationContext
import server.circlehelp.auth.Admin
import server.circlehelp.auth.User
import server.circlehelp.entities.base.IdObjectBase

@Entity
class Appraisable(

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val submitter: User,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    val topic: Topic,

    @Id @GeneratedValue
    override val id: Long? = null,
) : IdObjectBase<Long>() {

    @Embedded
    var approvalData: ApprovalData = ApprovalData()
        protected set

    @Throws(IllegalStateException::class)
    fun appraise(approvalData: ApprovalData, applicationContext: ApplicationContext): Any {
        if (this.approvalData.approved != null) {
            throw IllegalStateException(
                "Object already ${approvedBooleanToString(this.approvalData.approved)}."
            )
        }

        val result = when(approvalData.approved) {
            null -> throw IllegalArgumentException(
                "${ApprovalData::class.qualifiedName}.${ApprovalData::approved.name} was null."
            )

            true -> {
                topic.submit(applicationContext)
            }

            false -> {
                topic.reject(applicationContext)
            }
        }

        this.approvalData = approvalData

        return result
    }

    @Embeddable
    class ApprovalData(
        @ManyToOne(fetch = FetchType.LAZY)
        val appraiser: Admin? = null,

        val approved: Boolean? = null,

        @Column(nullable = false, length = 255)
        val reason: String = "",
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
}