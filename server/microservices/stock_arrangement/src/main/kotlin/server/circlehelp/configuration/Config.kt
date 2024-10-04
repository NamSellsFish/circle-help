package server.circlehelp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {

/**
* Required to deserialize for data classes JSON bodies.
* @author Khoa Anh Pham
*/
    @Bean
    fun jackson2ObjectMapperBuilder() =
        Jackson2ObjectMapperBuilderCustomizer { builder -> builder.configure(ObjectMapper().registerKotlinModule()) }
}