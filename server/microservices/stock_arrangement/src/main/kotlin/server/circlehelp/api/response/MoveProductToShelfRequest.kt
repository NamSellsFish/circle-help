package server.circlehelp.api.response

data class MoveProductToShelfRequest(val src: String,
                                     val des: CompartmentPosition)
