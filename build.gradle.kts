import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    kotlin("plugin.jpa") version "1.6.10"
}

group = "inno.tech"
version = "0.1.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    //core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots-spring-boot-starter")

    //database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    //kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    //logging
    implementation("io.github.microutils:kotlin-logging-jvm")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    dependencies {
        dependency("org.telegram:telegrambots-spring-boot-starter:5.7.1")
        dependency("io.github.microutils:kotlin-logging-jvm:2.1.20")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

//disable building plain archive at all
tasks.named<Jar>("jar") {
    enabled = false
}