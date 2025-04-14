plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.cooperlyt.cloud.addons"
version = "4.0.15"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))


    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    api("jakarta.validation:jakarta.validation-api")

    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-autoconfigure")


    compileOnly("org.springframework.data:spring-data-r2dbc")

    compileOnly("org.springframework:spring-web")
    compileOnly("org.slf4j:slf4j-api")

    compileOnly("org.springframework:spring-webflux")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = false
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