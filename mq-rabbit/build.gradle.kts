plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.cooperlyt.cloud.addons"
version = "1.1.2"

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
    api("org.springframework.cloud:spring-cloud-starter-stream-rabbit")

//    api("org.springframework.cloud:spring-cloud-stream-binder-rabbit")
//
//    api("org.springframework:spring-messaging")
//    api("org.springframework.amqp:spring-rabbit")
//    api("org.springframework.amqp:spring-amqp")

    api("io.projectreactor:reactor-core")
//    api("com.fasterxml.jackson.core:jackson-databind")

}


tasks.bootJar {
    enabled = false
}

tasks.jar {
    archiveClassifier.set("") // 移除 -plain 后缀
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
            //artifact(tasks.jar) // 明确指定 jar 任务的输出
        }
    }
    repositories {
        mavenLocal() // Publish to local Maven repository
    }
}