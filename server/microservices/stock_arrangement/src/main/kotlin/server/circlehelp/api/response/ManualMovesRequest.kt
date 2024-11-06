package server.circlehelp.api.response

data class ManualMovesRequest(
    val src: Iterable<String>,
    val des: Iterable<String?>,
    )
