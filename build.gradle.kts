plugins {
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    val kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.0-M1"
}

group = "org.itxtech"
version = "1.0.0"
description = "The Websocket API Server for Mirai Console"

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("kotlin.Experimental")
        }
    }
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    // External Dependencies
    compileOnly("org.itxtech:mcl:2.0.0-beta.2")
    compileOnly("net.mamoe:mirai-console-terminal:2.11.0-M2.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Embedded Dependencies
    implementation("io.ktor:ktor-websockets:1.6.8")
}

mavenCentralPublish {
    singleDevGithubProject("iTXTech", "soyuz")
    licenseAGplV3()
    useCentralS01()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}
