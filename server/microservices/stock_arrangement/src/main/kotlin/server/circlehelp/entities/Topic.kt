package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.PrimaryKeyJoinColumn
import org.springframework.context.ApplicationContext
import server.circlehelp.entities.base.IdObjectBase

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Topic(
    @jakarta.persistence.Id @GeneratedValue
    override val id: Long? = null
) : IdObjectBase<Long>() {

    @Throws(IllegalStateException::class)
    abstract fun submit(applicationContext: ApplicationContext): Any

    @Throws(IllegalStateException::class)
    open fun reject(applicationContext: ApplicationContext): Any = Unit
}