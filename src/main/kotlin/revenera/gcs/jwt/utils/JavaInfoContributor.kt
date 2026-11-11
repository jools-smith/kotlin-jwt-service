package revenera.gcs.jwt.utils

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.system.JavaVersion
import org.springframework.stereotype.Component

@Component
class JavaInfoContributor : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("java", mapOf(
                "version" to JavaVersion.getJavaVersion()))
    }
}