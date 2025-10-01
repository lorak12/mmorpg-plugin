import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("com.gradleup.shadow") version "9.0.1"
    kotlin("jvm") version "1.9.21"
}

group = "org.nakii.mmorpg"
version = "1.0-SNAPSHOT"
description = "MMORPG Core Plugin"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://jitpack.io")
    maven("https://repo.md-5.net/content/groups/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")

    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")

    implementation("net.objecthunter:exp4j:0.4.8")
    shadow("net.objecthunter:exp4j:0.4.8")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(project.configurations.runtimeClasspath.get())

        // Relokacja biblioteki żeby uniknąć konfliktów
        relocate("net.objecthunter.exp4j", "org.nakii.mmorpg.libs.exp4j")

        destinationDirectory.set(file("C:\\Users\\karol\\AppData\\Roaming\\.feather\\player-server\\servers\\66309597-64f4-441d-978f-06ed34fbee37\\plugins"))
    }

    build {
        dependsOn(shadowJar)
    }
}