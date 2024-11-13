package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonAlias

data class CompartmentInfo(val shelfNo: String,
                           @JsonAlias("Row") val rowNo: Int,
                           @JsonAlias("Compartment") val compartmentNo: Int,
                           val compartmentNoFromUserPerspective: String) {
}