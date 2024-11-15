package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.util.concurrent.Executor

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
class Config {

    private val logger = LoggerFactory.getLogger(Config::class.java)

    init {
        val runtime = Runtime.getRuntime()
        logger.info("Max Memory: ${runtime.maxMemory()}")
        logger.info("Processors: ${runtime.availableProcessors()}")
    }

    /**
    * Required to deserialize for data classes JSON bodies.
    * @author Khoa Anh Pham
    */
    @Bean
    @Autowired
    fun jackson2ObjectMapperBuilder() =
        Jackson2ObjectMapperBuilderCustomizer { builder -> builder.configure(ObjectMapper().registerKotlinModule()) }

    @Bean(BeanQualifiers.computationScheduler)
    fun computationScheduler() : Scheduler {
        return Schedulers.computation()
    }

    @Bean(BeanQualifiers.sameThreadScheduler)
    fun sameThreadScheduler() : Scheduler {
        return Schedulers.from { it.run() }
    }

}