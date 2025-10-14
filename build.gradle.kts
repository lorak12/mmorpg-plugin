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
    maven { name = "papermc"; url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { name = "citizens-repo"; url = uri("https://maven.citizensnpcs.co/repo") }
    maven { name = "sonatype"; url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { name = "dmulloy2-repo"; url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { name = "jitpack"; url = uri("https://jitpack.io") }

    // BQ / BetonQuest
    maven { name = "betonquest-repo"; url = uri("https://nexus.betonquest.org/repository/betonquest/") }
    maven { name = "enginehub-repo"; url = uri("https://maven.enginehub.org/repo/") }
    maven { name = "codemc-repo"; url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { name = "minebench-repo"; url = uri("https://repo.minebench.de/") }

    // EffectLib
    maven { name = "elmakers"; url = uri("https://maven.elmakers.com/repository/") }
    maven { name = "elmakers-github"; url = uri("https://maven.pkg.github.com/Slikey/EffectLib") }

    // Lumine / MythicLib
    maven { name = "lumine-releases"; url = uri("https://mvn.lumine.io/repository/maven-public/") }

    // VaultAPI alternative (hc-public-releases)
    maven { name = "hc-public-releases"; url = uri("https://nexus.hc.to/content/repositories/pub_releases/") }
    maven { name = "fancyinnovations"; url = uri("https://repo.fancyinnovations.com/releases") }
    maven { name = "pyr-snapshots"; url = uri("https://repo.pyr.lol/snapshots") }

}

dependencies {
    // === Paper / Bukkit ===
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.8")

    // === WorldEdit / WorldGuard ===
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")
    compileOnly("com.sk89q.worldguard:worldguard-core:7.0.9")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")

    // === Citizens ===
    compileOnly("net.citizensnpcs:citizens-main:2.0.40-SNAPSHOT")

    // === Holographic Displays / Decent Holograms ===
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.7")

    // === ProtocolLib + PacketWrapper ===
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    implementation("com.comphenix.packetwrapper:PacketWrapper:1.13-R0.1-SNAPSHOT")

    // === FancyNPCs / zNPCsPlus ===
    compileOnly("de.oliver:FancyNpcs:2.4.2")
    compileOnly("lol.pyr:znpcsplus-api:2.1.0-SNAPSHOT")

    // === Vault ===
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // === EffectLib ===
    compileOnly("com.elmakers.mine.bukkit:EffectLib:10.3")

    // === Utility / Core Libraries ===
    implementation("net.objecthunter:exp4j:0.4.8")
    compileOnly("com.mojang:authlib:3.13.56")
    compileOnly("org.apache.logging.log4j:log4j-api:3.0.0-beta2")
    compileOnly("org.apache.logging.log4j:log4j-core:3.0.0-beta2")
    implementation("org.apache.maven:maven-artifact:4.0.0-beta-3")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.json:json:20240303")
    implementation("com.cronutils:cron-utils:9.2.1")

    // === Integrations ===
    compileOnly("me.clip:placeholderapi:2.11.6")

    // === Misc ===
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("de.themoep:minedown-adventure:1.7.4-SNAPSHOT")
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

        // Core libraries
        relocate("net.objecthunter.exp4j", "org.nakii.mmorpg.libs.exp4j")

        // BetonQuest-style relocations
        relocate("de.slikey", "org.nakii.mmorpg.libs.effectlib")
        relocate("org.bstats", "org.nakii.mmorpg.libs.bq.bstats")
        relocate("org.apache.commons", "org.nakii.mmorpg.libs.bq.apachecommons")
        // ⚠️ Guava nie jest relokowana, żeby uniknąć konfliktu z Paper
        // relocate("com.google.common", "org.nakii.mmorpg.libs.bq.guavacommon")
        relocate("org.json", "org.nakii.mmorpg.libs.bq.json")
        relocate("io.papermc.lib", "org.nakii.mmorpg.libs.bq.paperlib")
        relocate("com.cronutils", "org.nakii.mmorpg.libs.bq.cronutils")
        relocate("de.themoep", "org.nakii.mmorpg.libs.bq.themoep")

        destinationDirectory.set(file("C:\\Users\\karol\\AppData\\Roaming\\.feather\\player-server\\servers\\66309597-64f4-441d-978f-06ed34fbee37\\plugins"))
    }

    build {
        dependsOn(shadowJar)
    }
}