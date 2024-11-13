package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.log

@Configuration
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
    fun jackson2ObjectMapperBuilder() =
        Jackson2ObjectMapperBuilderCustomizer { builder -> builder.configure(ObjectMapper().registerKotlinModule()) }

    /**
     * Rx Scheduler with fixed number of threads.
     */
    @Bean
    fun scheduler() : Scheduler {
        return Schedulers.from(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
    }
}