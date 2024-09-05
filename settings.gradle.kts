plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "cloud-addons"
include("keycloak-auth")
include("response")
include("serialize")
include("keycloak-admin")
