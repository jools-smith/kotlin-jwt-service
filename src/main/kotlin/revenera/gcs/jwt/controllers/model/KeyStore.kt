package revenera.gcs.jwt.controllers.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import revenera.gcs.jwt.AppProperties
import java.io.File
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger


@Component
data class KeyStore (
    private val props: AppProperties,
    val file : File = File(props.storagePath)//"C:\\Working\\kotlin\\spring-rest-service\\storage\\key-store.json")
    ) {
    private val logger = LoggerFactory.getLogger(KeyStore::class.java)
    private val lock = Any()
    private val json = ObjectMapper()
    private val store = LinkedList<KeyPairData>()

    private val counter = AtomicInteger(0)

    @PostConstruct
    fun postConstruct() {
        logger.info("postConstruct")
        load()
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("preDestroy")
        save()
    }

    fun load() {
        if (file.exists()) {
            synchronized(lock) {
                val keys : Set<KeyPairData> = json.readValue(this.file)

                this.store.clear()
                this.store.addAll(keys)

                this.counter.set(0)
            }
        }
    }

    fun save() {
        if (this.counter.get() > 0) {
            synchronized(lock) {
                json.writerWithDefaultPrettyPrinter().writeValue(this.file,store)

                this.counter.set(0)
            }
        }
    }

    fun data() : List<KeyPairData> {
        synchronized(lock) {
            return this.store
        }
    }

    fun get(id : String) : KeyPairData? {
        synchronized(lock) {
            return this.store.firstOrNull { it.id == id }
        }
    }

    fun add(keys : KeyPairData) : Boolean {
        synchronized(lock) {
            if (this.store.firstOrNull { it.id == keys.id } == null) {
                this.store.add(keys)

                this.counter.incrementAndGet()

                return true
            }
            return false
        }
    }
}