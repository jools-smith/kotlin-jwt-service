package revenera.gcs.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.configuration")
data class AppProperties(
    val storagePath: String
)