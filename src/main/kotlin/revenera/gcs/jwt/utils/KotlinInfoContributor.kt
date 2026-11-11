package revenera.gcs.jwt.utils

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class KotlinInfoContributor : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail(
            "kotlin",  mapOf(
                "version" to KotlinVersion.CURRENT.toString()))
    }
}