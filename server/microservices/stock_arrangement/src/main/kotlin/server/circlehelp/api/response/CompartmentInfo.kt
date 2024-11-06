package server.circlehelp.api.response

import com.fasterxml.jackson.annotation.JsonAlias

data class CompartmentInfo(@JsonAlias("Shelf") val shelfNo: Int,
                           @JsonAlias("Row") val rowNo: Int,
                           @JsonAlias("Compartment") val compartmentNo: Int,
                           val compartmentNoFromUserPerspective: Int) {
    constructor(coords: Iterable<Int>) : this(coords.iterator())

    private constructor(iterator: Iterator<Int>) : this(iterator.next(), iterator.next(), iterator.next(), iterator.next())
}