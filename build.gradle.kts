plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.20"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "io.github.cooperlyt"
version = "4.0.1"

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    //ext["misCloudVersion"] = "2.3.0"
    extra["springCloudVersion"] = "2023.0.3"

    repositories {
        mavenLocal()
        mavenCentral()
    }

//    dependencyManagement {
//        imports {
//            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
//        }
//    }

    dependencies {

//        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//        implementation("org.jetbrains.kotlin:kotlin-reflect")
//        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

        //implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    }

}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}