plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.cooperlyt.cloud.addons"
version = "4.0.13"

repositories {
    mavenCentral()
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    api("io.projectreactor:reactor-core")

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

tasks.jar {
    archiveClassifier.set("") // 移除 -plain 后缀
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