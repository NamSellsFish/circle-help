package server.circlehelp.api

const val baseURL = "/api"
const val test = "/test"
const val admin = "/admin"

fun Boolean.complement() : Boolean {
    return ! this
}
