plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.24"
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    `maven-publish`
}

group = "io.github.cooperlyt.cloud.addons"
version = "4.0.4"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("io.projectreactor:reactor-core")

    testImplementation(kotlin("test"))

    compileOnly("org.springframework:spring-webflux")
    compileOnly("jakarta.servlet:jakarta.servlet-api")

}


tasks.bootJar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal() // Publish to local Maven repository
    }
}