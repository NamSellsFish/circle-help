package server.circlehelp.services

import jakarta.persistence.PostLoad
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import server.circlehelp.delegated_classes.Autowirable

@Service
class AutowirableAutowireService(
    private val applicationContext: ApplicationContext
) {

    @PostLoad
    private fun autowire(autowirable: Autowirable) {
        autowirable.autowireApplicationContext(applicationContext)
    }
}