plugins {
    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    val kotlinVersion = "1.6.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version "1.6.20-RC"

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
    compileOnly("net.mamoe:mirai-console-terminal:2.11.0-M1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

    // Embedded Dependencies
    implementation("io.ktor:ktor-websockets:1.6.8")
    implementation("io.ktor:ktor-server-netty:1.6.8")
    implementation("io.ktor:ktor-server-core:1.6.8")
}

tasks.create<Jar>("fatJar") {
    archiveClassifier.set("all")
    dependsOn("jar")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveFileName.set("soyuz-${project.version}-all.jar")

    manifest {
        attributes["Name"] = "iTXTech Soyuz"
        attributes["Revision"] = Runtime.getRuntime().exec("git rev-parse --short HEAD")
            .inputStream.bufferedReader().readText().trim()
    }

    val list = ArrayList<Any>()

    configurations.compileClasspath.get().copyRecursive().forEach { file ->
        arrayOf("ktor-websockets", "ktor-server").forEach {
            if (file.absolutePath.contains(it)) {
                list.add(zipTree(file))
            }
        }
    }

    from(list, sourceSets.main.get().output)
}

tasks {
    "assemble" {
        dependsOn("fatJar")
    }
}

mavenCentralPublish {
    singleDevGithubProject("iTXTech", "soyuz")
    licenseAGplV3()
    useCentralS01()

    publication {
        artifacts.artifact(tasks.getByName("fatJar"))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}
