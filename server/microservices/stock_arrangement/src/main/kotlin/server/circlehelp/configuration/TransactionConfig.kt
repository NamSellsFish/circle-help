package server.circlehelp.configuration

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class TransactionConfig {

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory) : PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}