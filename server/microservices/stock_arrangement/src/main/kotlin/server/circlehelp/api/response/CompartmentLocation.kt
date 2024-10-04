package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonAlias

data class CompartmentLocation(@JsonAlias("Shelf") val shelfNumber: Int,
                               @JsonAlias("Row") val rowNumber: Int,
                               @JsonAlias("Compartment") val compartmentNumber: Int)
