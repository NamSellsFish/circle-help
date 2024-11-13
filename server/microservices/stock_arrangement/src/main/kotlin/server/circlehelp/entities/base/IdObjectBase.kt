package server.circlehelp.entities.base

abstract class IdObjectBase<ID : Number> : IdObject<ID> {

    @Throws(UnsupportedOperationException::class)
    override fun equals(other: Any?): Boolean {
        return equalsID(other)
    }

    override fun hashCode(): Int {
        return hashCodeID()
    }

}