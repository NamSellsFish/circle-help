package server.circlehelp.entities

import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import lombok.EqualsAndHashCode
import org.springframework.context.ApplicationContext
import server.circlehelp.delegated_classes.Autowirable
import server.circlehelp.services.AutowirableAutowireService

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AutowirableAutowireService::class)
abstract class Topic(
    @jakarta.persistence.Id @GeneratedValue
    @EqualsAndHashCode.Include val id: Long? = null
) : Autowirable {

    @Transient
    protected lateinit var applicationContext: ApplicationContext

    @Throws(IllegalStateException::class)
    abstract fun submit(): Any

    @Throws(IllegalStateException::class)
    open fun reject(): Any = Unit

    override fun autowireApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}