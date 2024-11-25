package server.circlehelp.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.std.CalendarSerializer
import com.fasterxml.jackson.databind.ser.std.SqlDateSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.text.SimpleDateFormat
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
    fun jackson2ObjectMapperBuilder() =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            builder.configure(
                ObjectMapper()
                    .registerKotlinModule()
                    .registerModule(JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .setDateFormat(df)
            )
        }

    @Bean
    fun objectMapper(): ObjectMapper {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDateFormat(df)
    }

    @Bean(BeanQualifiers.computationScheduler)
    fun computationScheduler() : Scheduler {
        return Schedulers.computation()
    }

    @Bean(BeanQualifiers.sameThreadScheduler)
    fun sameThreadScheduler() : Scheduler {
        return Schedulers.from { it.run() }
    }

}