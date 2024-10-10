package io.github.cooperlyt.cloud.addons.keycloak.auth

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector
import com.nimbusds.jose.proc.JWSKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector
import com.nimbusds.jwt.proc.JWTProcessor
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.oauth2.core.*
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI
import java.security.Key
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@FunctionalInterface
interface TenantJWKSUriProvider {

    fun findJWKSUri(issuer: String): String?
}

class TenantJWSKeySelector(private val tenants: TenantJWKSUriProvider) : JWTClaimsSetAwareJWSKeySelector<SecurityContext> {


    private val selectors: MutableMap<String, JWSKeySelector<SecurityContext>> = ConcurrentHashMap()

    override fun selectKeys(jwsHeader: JWSHeader, jwtClaimsSet: JWTClaimsSet, securityContext: SecurityContext?): List<Key?> {
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

class TenantJwtIssuerValidator(private val tenants: TenantJWKSUriProvider) : OAuth2TokenValidator<Jwt> {
    private val error: OAuth2Error = OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN, "The iss claim is not valid",
        "https://tools.ietf.org/html/rfc6750#section-3.1")

    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        return if (tenants.findJWKSUri(token.issuer.toString()) != null)
            OAuth2TokenValidatorResult.success() else OAuth2TokenValidatorResult.failure(error)
    }
}


@Configuration
@ConditionalOnBean(TenantJWKSUriProvider::class)
@ConditionalOnClass(HttpServletRequest::class)
class OAuthTenantConfigure {


    @Bean
    fun keySelector(tenants: TenantJWKSUriProvider): JWTClaimsSetAwareJWSKeySelector<SecurityContext>{
        return TenantJWSKeySelector(tenants)
    }

    @Bean
    fun tokenValidator(tenants: TenantJWKSUriProvider): OAuth2TokenValidator<Jwt>{
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


class JwtToClaimsSetConverter(private val jwtProcessor: JWTProcessor<SecurityContext>) :
    Converter<JWT, Mono<JWTClaimsSet>> {
    override fun convert(jwt: JWT): Mono<JWTClaimsSet> {
        return Mono.fromCallable {
            jwtProcessor.process(
                jwt,
                null
            )
        }
    }
}


@FunctionalInterface
interface ReactiveTenantIssuerValidator {

    fun valid(issuer: String): Mono<Boolean>
}

class RegexTenantIssuerValidator(private val regex: String): ReactiveTenantIssuerValidator {

    override fun valid(issuer: String): Mono<Boolean> {
        return Mono.just(regex.toRegex().matches(issuer))
    }


}

class ReactiveAuthenticationManagerProvider(private val tenantJWKSUriProvider: ReactiveTenantIssuerValidator) {

    private val authenticationManagers: MutableMap<String, ReactiveAuthenticationManager> = mutableMapOf()


    fun getAuthenticationManager(issuer: String): Mono<ReactiveAuthenticationManager> {
        return authenticationManagers[issuer]?.let {
            Mono.just(it)
        } ?: tenantJWKSUriProvider.valid(issuer)
            .filter{ it }
            .flatMap { addManager(issuer) }
    }

    private fun addManager(issuer: String): Mono<JwtReactiveAuthenticationManager> {
        return Mono.fromCallable { ReactiveJwtDecoders.fromIssuerLocation(issuer) }
            .subscribeOn(Schedulers.boundedElastic())
            .map { jwtDecoder -> JwtReactiveAuthenticationManager(jwtDecoder) }
            .doOnNext { authenticationManager ->
                authenticationManager.setJwtAuthenticationConverter(KeycloakReactiveAuthenticationConverter())
                authenticationManagers[issuer] = authenticationManager
            }
    }
}


@Configuration
@ConditionalOnClass(WebFluxConfigurer::class)
class OAuthReactiveTenantConfigure{


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.addons.keycloak.oauth2" , name = ["issuer-regex"])
    fun regexTenantIssuerValidator(@Value("\${spring.addons.keycloak.oauth2.issuer-regex}") regex: String): RegexTenantIssuerValidator {
        return RegexTenantIssuerValidator(regex)
    }

    @Bean
    @ConditionalOnBean(ReactiveTenantIssuerValidator::class)
    @Lazy
    fun authenticationManagerProvider(tenantJWKSUriProvider: ReactiveTenantIssuerValidator): ReactiveAuthenticationManagerProvider{
        return ReactiveAuthenticationManagerProvider(tenantJWKSUriProvider)
    }


}

