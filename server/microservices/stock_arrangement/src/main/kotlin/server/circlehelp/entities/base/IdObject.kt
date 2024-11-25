package server.circlehelp.entities.base

import kotlin.jvm.Throws

/**
 * Equality checking for objects with generated IDs.
 * Invalid if either objects' ID is null.
 */
interface IdObject<ID : Number> {

    val id: ID?

    @Throws(UnsupportedOperationException::class)
    fun equalsID(other: Any?) : Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdObject<*>

        if (id == null)
            throw UnsupportedOperationException("this.id")

        if (other.id == null)
            throw UnsupportedOperationException("other.id")

        return id == other.id
    }

    fun hashCodeID() : Int {
        return id?.toInt() ?: 0
    }
}