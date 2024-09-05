package io.github.cooperlyt.cloud.addons.keycloak.auth

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector
import com.nimbusds.jwt.proc.JWTProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.security.Key

interface TenantRepository {

    fun findJWKSUri(issuer: String): String?
}


class TenantJWSKeySelector(private val tenants: TenantRepository) : JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private val selectors: MutableMap<String, JWSKeySelector<SecurityContext>> = ConcurrentHashMap()

    override fun selectKeys(jwsHeader: JWSHeader?, jwtClaimsSet: JWTClaimsSet, securityContext: SecurityContext): List<Key?> {
        return selectors.computeIfAbsent(toTenant(jwtClaimsSet)) { tenant: String -> fromTenant(tenant) }
            .selectJWSKeys(jwsHeader, securityContext)
    }

    private fun toTenant(claimSet: JWTClaimsSet): String {
        return claimSet.getClaim("iss") as String
    }

    private fun fromTenant(tenant: String): JWSKeySelector<SecurityContext> {
        return Optional.ofNullable(this.tenants.findJWKSUri(tenant))
            .map { uri: String -> fromUri(uri) }
            .orElseThrow { IllegalArgumentException("unknown tenant") }
    }

    private fun fromUri(uri: String): JWSKeySelector<SecurityContext> {
        return try {
            JWSAlgorithmFamilyJWSKeySelector.fromJWKSetURL(URI(uri).toURL())// URL(uri)
        } catch (ex: Exception) {
            throw IllegalArgumentException(ex)
        }
    }
}

class TenantJwtIssuerValidator(private val tenants: TenantRepository) : OAuth2TokenValidator<Jwt> {
    private val error: OAuth2Error = OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN, "The iss claim is not valid",
        "https://tools.ietf.org/html/rfc6750#section-3.1")

    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        return if (tenants.findJWKSUri(token.issuer.toString()) != null)
            OAuth2TokenValidatorResult.success() else OAuth2TokenValidatorResult.failure(error)
    }
}


@Configuration
@ConditionalOnBean(TenantRepository::class)
class TenantConfigure {


    @Bean
    fun keySelector(tenants: TenantRepository): JWTClaimsSetAwareJWSKeySelector<SecurityContext>{
        return TenantJWSKeySelector(tenants)
    }

    @Bean
    fun tokenValidator(tenants: TenantRepository): OAuth2TokenValidator<Jwt>{
        return TenantJwtIssuerValidator(tenants)
    }


    @Bean
    fun jwtProcessor(keySelector: JWTClaimsSetAwareJWSKeySelector<SecurityContext>): JWTProcessor<SecurityContext> {
        val jwtProcessor = DefaultJWTProcessor<SecurityContext>()
        jwtProcessor.jwtClaimsSetAwareJWSKeySelector = keySelector
        return jwtProcessor
    }


    @Bean
    fun jwtDecoder(jwtProcessor: JWTProcessor<SecurityContext>?, jwtValidator: OAuth2TokenValidator<Jwt>?): JwtDecoder {
        val decoder = NimbusJwtDecoder(jwtProcessor)
        val validator: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(JwtValidators.createDefault(), jwtValidator)
        decoder.setJwtValidator(validator)
        return decoder
    }

}

