package io.github.cooperlyt.cloud.addons.keycloak.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

class KeycloakAuthenticationConverter(
    private var clientId: String? = null
) : Converter<Jwt, JwtAuthenticationToken> {

    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> = JwtGrantedAuthoritiesConverter()

    @Suppress("UNCHECKED_CAST")
    private fun authenticationRoles(jwt: Jwt): Collection<GrantedAuthority> {

        if (clientId == null) {
            clientId = jwt.getClaim("azp")
        }

        val authorities = jwtGrantedAuthoritiesConverter.convert(jwt)?.toMutableSet() ?: mutableSetOf()

        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access")
        val resource = resourceAccess?.get(clientId) as? Map<String, Any>
        val resourceRoles = resource?.get("roles") as? Collection<String>

        resourceRoles?.let {
            authorities.addAll(it.map { role -> SimpleGrantedAuthority("ROLE_$role") })
        }

        return authorities
    }

    override fun convert(jwt: Jwt): JwtAuthenticationToken {
        return JwtAuthenticationToken(jwt, authenticationRoles(jwt))
    }
}