package server.circlehelp.api.response

data class ManualMoveRequest(val src: Long,
                             val des: CompartmentPosition)
