package revenera.gcs.jwt.controllers.model

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class KeyPairData(
    val id: String = "",
    val publicKey: ByteArray = byteArrayOf(),
    val privateKey: ByteArray = byteArrayOf()) {

    private val logger = LoggerFactory.getLogger(KeyPairData::class.java)

    private fun reconstructKeyPair() : KeyPair {
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicKey = keyFactory.generatePublic(
            X509EncodedKeySpec(this.publicKey)
        )

        val privateKey = keyFactory.generatePrivate(
            PKCS8EncodedKeySpec(this.privateKey)
        )

        return KeyPair(publicKey, privateKey)
    }

    private fun createBasicClaim(issuer:String, duration:Long, unit:ChronoUnit) : JWTClaimsSet.Builder {
        val now = Instant.now()

        return JWTClaimsSet.Builder()
            .issuer(issuer)
            .notBeforeTime(Date.from(now))
            .issueTime(Date.from(now))
            .expirationTime(Date.from(
                now.plus(duration, unit)
            ))
    }

    private fun signJwt(jwt : SignedJWT) : String {
        val kp = reconstructKeyPair()

        val signer = RSASSASigner(kp.private)

        jwt.sign(signer)

        return jwt.serialize()
    }

    private fun keyToPem(data : ByteArray): String {
        val base64 = Base64
            .getMimeEncoder(64, "\n".toByteArray())
            .encodeToString(data)

        return """
            -----BEGIN PRIVATE KEY-----
            $base64
            -----END PRIVATE KEY-----
            """.trimIndent()
    }

    private fun keyToPublicPem(data : ByteArray): String {
        val base64 = Base64
            .getMimeEncoder(64, "\n".toByteArray())
            .encodeToString(data)

        return """
            -----BEGIN PUBLIC KEY-----
            $base64
            -----END PUBLIC KEY-----
            """.trimIndent()
    }

    fun privateKeyToPem(): String = keyToPem(this.privateKey)

    fun publicKeyToPem(): String = keyToPublicPem(this.publicKey)

//    fun privateKeyToPem(): String {
//        val base64 = Base64
//            .getMimeEncoder(64, "\n".toByteArray())
//            .encodeToString(this.privateKey)
//
//        return """
//            -----BEGIN PRIVATE KEY-----
//            $base64
//            -----END PRIVATE KEY-----
//            """.trimIndent()
//    }


    fun makeClientJwt(issuer:String, instances:List<String>, duration:Long=3600L, unit : ChronoUnit = ChronoUnit.SECONDS) : String = signJwt(SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .build(),
            createBasicClaim(issuer, duration, unit)
                .audience(instances)
                .claim("cnf", linkedMapOf(
                    "jwk" to linkedMapOf(
                        "kid" to this.id,
                        "kty" to  "CLIENT")))
                .build()
        ))

    fun makeAdminJwt(issuer:String, duration:Long=3600L, unit : ChronoUnit = ChronoUnit.SECONDS) : String = signJwt(SignedJWT(
             JWSHeader.Builder(JWSAlgorithm.RS256)
                 .keyID(this.id)
                 .type(JOSEObjectType.JWT)
                 .build(),
            createBasicClaim(issuer, duration, unit)
                .claim("cnf", linkedMapOf(
                    "jwk" to linkedMapOf(
                        "kid" to this.id,
                        "kty" to  "ADMIN")))
                .build()
        ))

    fun makeClsJwt(issuer:String, duration:Long=3600L, unit : ChronoUnit = ChronoUnit.SECONDS) : String = signJwt(SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .type(JOSEObjectType.JWT)
            .build(),
            createBasicClaim(issuer, duration, unit)
                .claim("sub", "Authorization")
                .claim("roles", listOf("ROLE_CAPABILITY"))
                .build()
        ))

    companion object {
        fun create(id: String): KeyPairData {
            val generator = KeyPairGenerator.getInstance("RSA")

            generator.initialize(2048, SecureRandom())

            val kp = generator.generateKeyPair()

            return KeyPairData(id, kp.public.encoded, kp.private.encoded)
        }
    }
}