package server.circlehelp.api.request

data class AppraisalDecisionRequest(
    val appraisableId: Long,
    val approved: Boolean,
    val reason: String = ""
)
