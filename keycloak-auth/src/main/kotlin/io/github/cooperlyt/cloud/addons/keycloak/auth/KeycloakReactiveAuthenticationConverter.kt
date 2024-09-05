package io.github.cooperlyt.cloud.addons.keycloak.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono

class KeycloakReactiveAuthenticationConverter: Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(KeycloakReactiveAuthenticationConverter::class.java)

        private const val ROLE_PREFIX: String = "ROLE_"
        private const val SCOPE_PREFIX: String = "SCOPE_"
    }

    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
        JwtGrantedAuthoritiesConverter()

    private fun authenticationRoles(jwt: Jwt): Collection<GrantedAuthority> {

        logger.info("convert jwt to spring role")

        logger.trace("Claims: {}", jwt.claims)
        logger.trace("Header: {}", jwt.headers)
        logger.trace("token: {}", jwt.tokenValue)

        val authorities = jwtGrantedAuthoritiesConverter.convert(jwt)?.toMutableList() ?: mutableListOf()

        val resourceAccess = jwt.getClaim<Map<String, Map<String, Collection<String>>>>("resource_access")
        resourceAccess?.forEach { (key, value) ->
            value["roles"]?.let { roles ->
                authorities.addAll(roles.map { SimpleGrantedAuthority("$ROLE_PREFIX${key}_$it") })
            }
        }

        val realmAccess = jwt.getClaim<Map<String, Collection<String>>>("realm_access")
        val realmRoles = realmAccess?.get("roles")
        realmRoles?.let {
            authorities.addAll(it.map { role -> SimpleGrantedAuthority("$ROLE_PREFIX$role") })
        }

        val scope = jwt.getClaim<String>("scope")
        if (StringUtils.hasText(scope)) {
            authorities.addAll(scope.split("\\s+".toRegex()).map { SimpleGrantedAuthority("$SCOPE_PREFIX$it") })
        }

        return authorities
    }

    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> {
        return Mono.just(JwtAuthenticationToken(jwt, this.authenticationRoles(jwt)))
    }
}