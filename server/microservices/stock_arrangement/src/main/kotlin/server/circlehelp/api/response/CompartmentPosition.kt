package server.circlehelp.api.response

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains

data class CompartmentPosition(val rowNo: Int,
                               val compartmentNo: Int) {
    constructor(coords: Iterable<Int>) : this(coords.iterator())

    private constructor(iterator: Iterator<Int>) : this(iterator.next(), iterator.next())

    fun asIterable() = listOf(rowNo, compartmentNo)

    companion object {
        fun matchesJsonNode(node: JsonNode) : Boolean {
            return node.contains(CompartmentPosition::rowNo.name)
                    && node.contains(CompartmentPosition::compartmentNo.name)
                    // && node.size() == 2
        }
    }
}

