package server.circlehelp.api.response

import server.circlehelp.api.request.AppraisalDecisionRequest

data class AppraisalDecisionResponse(
    val request: AppraisalDecisionRequest,
    val content: String
)