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
import server.circlehelp.services.TableAuditingService

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AutowirableAutowireService::class, TableAuditingService::class)
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

    @Throws(IllegalStateException::class)
    override fun autowireApplicationContext(applicationContext: ApplicationContext) {
        if (this::applicationContext.isInitialized)
            throw IllegalStateException("${this::applicationContext.name} already initialized")
        this.applicationContext = applicationContext
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Topic

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}