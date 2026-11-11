package revenera.gcs.jwt.controllers

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import revenera.gcs.jwt.utils.ServiceDiagnostics
import revenera.gcs.jwt.controllers.model.KeyPairData
import revenera.gcs.jwt.controllers.model.KeyStore

@RestController
@RequestMapping("/api")
class KeyController (
    private val keyStore : KeyStore,
    private val diagnostics: ServiceDiagnostics) {

    private val logger = LoggerFactory.getLogger(KeyController::class.java)

    @GetMapping("/keys")
    fun getKeys(request: HttpServletRequest) : List<KeyPairData> {
        diagnostics.createContext("get/keys").use {
            logger.trace(request.requestURI)

            return keyStore.data()
        }
    }

    @GetMapping("/keys/{id}")
    fun getKey(@PathVariable id : String, request: HttpServletRequest) : KeyPairData? {
        diagnostics.createContext("get/keys/{id}").use {
            logger.trace(request.requestURI)

            return this.keyStore.get(id)
        }
    }

    @GetMapping("/keys/{id}/public/der")
    fun getPublicKeyDer(@PathVariable id : String, request: HttpServletRequest) : ByteArray? {
        diagnostics.createContext("get/keys/{id}/public/der").use {
            logger.trace(request.requestURI)

            return this.keyStore.get(id)?.publicKey
        }
    }

    @GetMapping("/keys/{id}/private/pem")
    fun getPrivateKeyPem(@PathVariable id : String, request: HttpServletRequest) : String? {
        diagnostics.createContext("get/keys/{id}/private/pem").use {
            logger.trace(request.requestURI)

            return this.keyStore.get(id)?.privateKeyToPem()
        }
    }

    @GetMapping("/keys/{id}/public/pem")
    fun getPublicKeyPem(@PathVariable id : String, request: HttpServletRequest) : String? {
        diagnostics.createContext("get/keys/{id}/public/pem").use {
            logger.trace(request.requestURI)

            return this.keyStore.get(id)?.publicKeyToPem()
        }
    }

    @PostMapping("/keys/{id}")
    fun createKeyPair(@PathVariable id: String, request: HttpServletRequest) : KeyPairData? {
        diagnostics.createContext("post/keys/{id}").use {
            logger.trace(request.requestURI)

            this.keyStore.add(KeyPairData.create(id))

            return this.keyStore.get(id)
        }
    }


    @PostMapping("/keys/{id}/jwt/client")
    fun createClientJWT(@PathVariable id: String, @RequestBody instances: List<String>, request: HttpServletRequest) : String? {

        diagnostics.createContext("post/keys/{id}/jwt/client").use {
            logger.trace("create admin JWT {} {}", id, instances)

            return this.keyStore.get(id)?.makeClientJwt("arse", instances)
        }
    }

    @PostMapping("/keys/{id}/jwt/admin")
    fun createClientJWT(@PathVariable id: String, request: HttpServletRequest) : String? {

        diagnostics.createContext("post/keys/{id}/jwt/admin").use {
            logger.trace("create admin JWT {}", id)

            return this.keyStore.get(id)?.makeAdminJwt("arse")
        }
    }

    @PostMapping("/keys/{id}/jwt/cls")
    fun createClsJWT(@PathVariable id: String, request: HttpServletRequest) : String? {
        diagnostics.createContext("post/keys/{id}/jwt/cls").use {
            logger.trace("create cls JWT {}", id)

            return this.keyStore.get(id)?.makeClsJwt(id)
        }
    }

    @PostConstruct
    fun postConstruct() {
        logger.info("postConstruct")
    }

    @PreDestroy
    fun preDestroy() {
        logger.info("preDestroy")
    }
}