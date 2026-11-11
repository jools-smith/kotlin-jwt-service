package revenera.gcs.jwt.utils

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.TimeSource


data class ContextAggregate (
    val name : String,
    var count: Long = 0,
    var duration: Duration = Duration.ZERO) {

    fun update(value: Duration) {
        this.count++
        this.duration += value
    }

    fun asContextDisplay() : ContextDisplay {
        val secs = this.duration.inWholeNanoseconds / 1_000_000_000.0

        return ContextDisplay(
            this.name,
            this.count,
            secs,
            secs / this.count
        )
    }
}

data class ContextDisplay (
    val name : String,
    var count: Long,
    var duration: Double,
    var mu: Double)

@Component
final class ServiceDiagnostics {
    inner class Context(
        private val name: String,
        private val start: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()) : AutoCloseable {

        override fun close() {
            val duration = this.start.elapsedNow()

            val context = contexts.firstOrNull() { e -> e.name == this.name } ?: ContextAggregate(this.name)
            if (context.count == 0L) {
                contexts.add(context)
            }
            context.update(duration)
        }
    }

    private val logger = LoggerFactory.getLogger(ServiceDiagnostics::class.java)

    private val contexts = mutableListOf<ContextAggregate>()

    fun createContext(name: String) : Context = Context(name)

    fun getContexts() : List<ContextDisplay> = this.contexts.map { c -> c.asContextDisplay() }.toList()


    @PostConstruct
    fun postConstruct() {
        logger.info("postConstruct")
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("preDestroy")
    }
}