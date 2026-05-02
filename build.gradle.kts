import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    kotlin("plugin.jpa") version "2.3.21"
}

group = "inno.tech"
version = "0.2.11"
java.sourceCompatibility = JavaVersion.VERSION_25

repositories {
    mavenCentral()
}

dependencies {
    //core
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.telegram:telegrambots:6.9.7.1")

    //support telegrambots
    compileOnly("org.projectlombok:lombok")

    //database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.liquibase:liquibase-core")
    runtimeOnly("org.postgresql:postgresql")

    //kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    //logging
    implementation("io.github.microutils:kotlin-logging-jvm")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    dependencies {
        dependency("io.github.microutils:kotlin-logging-jvm:2.1.20")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_25)
    }
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
