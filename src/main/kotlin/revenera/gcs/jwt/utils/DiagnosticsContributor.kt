package revenera.gcs.jwt.utils

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class DiagnosticsContributor(
    val  diagnostics: ServiceDiagnostics) : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("diagnostics", diagnostics.getContexts())
    }
}