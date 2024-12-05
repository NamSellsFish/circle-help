package server.circlehelp.api.response

import server.circlehelp.entities.Appraisable
import server.circlehelp.entities.OrderSubmissionTopic
import java.time.LocalDateTime

data class SubmittedOrdersResponse(
    val submitterEmail: String,
    val json: String,
    val createdDate: LocalDateTime,
    val appraiserEmail: String,
    val approved: String,
    val reason: String,
    val approvalDate: LocalDateTime?,
) {

    companion object {
        fun fromAppraisable(appraisable: Appraisable): SubmittedOrdersResponse {
            return SubmittedOrdersResponse(
                appraisable.submitter.email,
                (appraisable.topic as OrderSubmissionTopic).json,
                appraisable.createdDate,
                appraisable.approvalData.appraiser?.email ?: "",
                Appraisable.approvedBooleanToString(appraisable.approvalData.approved),
                appraisable.approvalData.reason,
                appraisable.approvalData.approvalDate
            )
        }
    }
}
