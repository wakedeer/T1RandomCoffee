import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.github.ben-manes.versions") version "0.66.0"
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"
    kotlin("plugin.jpa") version "2.4.21"
}

group = "inno.tech"
version = "0.3.2"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_25)
        javaParameters.set(true)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:10.3.0")
    implementation("org.telegram:telegrambots-client:10.3.0")

    //database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.liquibase:liquibase-core:5.0.4")
    runtimeOnly("org.postgresql:postgresql:42.7.14")

    //kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //logging
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")

    //test
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.5"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//disable building plain archive at all
tasks.named<Jar>("jar") {
    enabled = false
}

//generate Build Information
springBoot {
    buildInfo()
}
