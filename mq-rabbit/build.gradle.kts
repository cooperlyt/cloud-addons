plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.cooperlyt.cloud.addons"
version = "1.1.1"

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")


    }
}

dependencies {


    testImplementation(kotlin("test"))

    api("org.springframework.boot:spring-boot-autoconfigure")

    api("org.slf4j:slf4j-api")

    compileOnly("org.springframework.cloud:spring-cloud-stream-binder-rabbit")

    compileOnly("org.springframework:spring-messaging")
    compileOnly("org.springframework.amqp:spring-rabbit")

    compileOnly("org.springframework.amqp:spring-amqp")

    compileOnly("io.projectreactor:reactor-core")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")

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