package server.circlehelp.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import server.circlehelp.api.serializers.EmptyAcceptingTimeDeserializer
import server.circlehelp.api.serializers.EmptyAcceptingTimeSerializer
import server.circlehelp.delegated_classes.DependencyNode
import server.circlehelp.entities.ArrivedPackage
import server.circlehelp.entities.Event
import server.circlehelp.entities.EventProduct
import server.circlehelp.entities.InventoryStock
import server.circlehelp.entities.PackageProduct
import server.circlehelp.entities.Product
import server.circlehelp.entities.ProductCategorization
import server.circlehelp.entities.ProductCategory
import server.circlehelp.entities.ProductOnCompartment
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.LocalTime

@Configuration
@EnableWebMvc
@EnableSpringDataWebSupport
@Slf4j
@EnableJpaAuditing
class Config {


    init {
        val runtime = Runtime.getRuntime()
        log.info("Max Memory: ${runtime.maxMemory()}")
        log.info("Processors: ${runtime.availableProcessors()}")
    }

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    /**
    * Required to deserialize for data classes JSON bodies.
    * @author Khoa Anh Pham
    */
    @Bean
    fun jackson2ObjectMapperBuilderCustomizer() =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            builder.configure(
                ObjectMapper()
                    .registerKotlinModule()
                    .registerModule(JavaTimeModule())
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    .setDateFormat(df)
                    .registerModule(SimpleModule()
                        .addSerializer(LocalTime::class.java, EmptyAcceptingTimeSerializer())
                        .addDeserializer(LocalTime::class.java, EmptyAcceptingTimeDeserializer()))
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            )
        }

    @Bean
    fun jackson2ObjectMapperBuilder() = Jackson2ObjectMapperBuilder.json().apply { configure(
        ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .registerModule(SimpleModule()
                .addSerializer(LocalTime::class.java, EmptyAcceptingTimeSerializer())
                .addDeserializer(LocalTime::class.java, EmptyAcceptingTimeDeserializer()))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    )}

    @Bean
    fun objectMapper(): ObjectMapper {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        return ObjectMapper()
            .registerModule(JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDateFormat(df)
            .registerModule(SimpleModule()
                .addSerializer(LocalTime::class.java, EmptyAcceptingTimeSerializer())
                .addDeserializer(LocalTime::class.java, EmptyAcceptingTimeDeserializer()))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
    }

    @Bean(BeanQualifiers.computationScheduler)
    fun computationScheduler() : Scheduler {
        return Schedulers.computation()
    }

    @Bean(BeanQualifiers.sameThreadScheduler)
    fun sameThreadScheduler() : Scheduler {
        return Schedulers.from { it.run() }
    }

    @Bean
    fun productDependencies() = DependencyNode(Product::class)

    @Bean
    fun productCategoryDependencies() = DependencyNode(ProductCategory::class)

    @Bean
    fun arrivedPackageDependencies() = DependencyNode(ArrivedPackage::class)

    @Bean
    fun eventDependencies() = DependencyNode(Event::class)

    @Bean
    fun productCategorizationDependencies(
        @Qualifier("productDependencies")
        productDependencies: DependencyNode,
        @Qualifier("productCategoryDependencies")
        productCategoryDependencies: DependencyNode
    ) = DependencyNode(ProductCategorization::class, listOf(productDependencies, productCategoryDependencies))

    @Bean
    fun eventProductDependencies(
        @Qualifier("productDependencies")
        productDependencies: DependencyNode,
        @Qualifier("eventDependencies")
        eventDependencies: DependencyNode,
    ) = DependencyNode(EventProduct::class, listOf(productDependencies, eventDependencies))

    @Bean
    fun packageProductDependencies(
        @Qualifier("productDependencies")
        productDependencies: DependencyNode,
        @Qualifier("arrivedPackageDependencies")
        arrivedPackageDependencies: DependencyNode,
    ) = DependencyNode(PackageProduct::class, listOf(productDependencies, arrivedPackageDependencies))

    @Bean
    fun productOnCompartmentDependencies(
        @Qualifier("packageProductDependencies")
        packageProductDependencies: DependencyNode
    ) = DependencyNode(ProductOnCompartment::class, listOf(packageProductDependencies))

    @Bean
    fun inventoryStockDependencies(
        @Qualifier("packageProductDependencies")
        packageProductDependencies: DependencyNode
    ) = DependencyNode(InventoryStock::class, listOf(packageProductDependencies))

    companion object {
        val log: Logger = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }
}