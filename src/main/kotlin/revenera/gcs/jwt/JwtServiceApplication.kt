package revenera.gcs.jwt

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import revenera.gcs.jwt.controllers.model.KeyStore
import revenera.gcs.jwt.utils.ServiceDiagnostics


@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class JwtServiceApplication(
    private val keyStore: KeyStore,
    private val diagnostics: ServiceDiagnostics) {
    private val logger = LoggerFactory.getLogger(JwtServiceApplication::class.java)

    @PostConstruct
    fun postConstruct() {
        logger.info("postConstruct")
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("preDestroy")
    }

    @Scheduled(cron = "0 * * * * *")
    fun housekeeping() {
        diagnostics.createContext("housekeeping").use {
            logger.debug("housekeeping started...")
        }
    }

    @Scheduled(cron = "*/10 * * * * *")
    fun serialization() {
        diagnostics.createContext("serialization").use {
            logger.debug("serialization started...")
            keyStore.save()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<JwtServiceApplication>(*args)
}
