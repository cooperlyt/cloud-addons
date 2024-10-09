package io.github.cooperlyt.cloud.addons.keycloak.auth

interface UserInfo {

    val id: String

    val givenName: String

    val familyName: String
}