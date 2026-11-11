package revenera.gcs.jwt.utils

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import revenera.gcs.jwt.controllers.model.KeyStore

@Component
class ServiceInfoContributor(
    val keyStore: KeyStore
) : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("keys", keyStore.data().map { k -> k.id })
    }
}