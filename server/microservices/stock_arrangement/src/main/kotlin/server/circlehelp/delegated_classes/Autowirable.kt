package server.circlehelp.delegated_classes

import org.springframework.context.ApplicationContext

interface Autowirable {
    fun autowireApplicationContext(applicationContext: ApplicationContext)
}