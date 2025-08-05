import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.nakii.mmorpg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Spigot repository
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    // FIXED: The correct repository for LibsDisguises, as per the official documentation.
    maven {
        name = "md5-repo"
        url = uri("https://repo.md-5.net/content/groups/public/")
    }
    // Repository for EffectLib
    maven {
        name = "elmakers-repo"
        url = uri("https://maven.elmakers.com/repository/")
    }
    // NEW: The repository for PacketEvents
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    // Spigot API - Provided by the server at runtime
    compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")

    // SQLite Driver - Bundled into our plugin by Shadow
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")

    // LibsDisguises API - Provided by the LibsDisguises plugin at runtime
    // FIXED: Using the correct group, name, and version from the official documentation.
    // NOTE: Switched from 'implementation' to 'compileOnly' which is correct for Spigot plugins.
    compileOnly(group = "me.libraryaddict.disguises", name = "libsdisguises", version = "11.0.0")

    // EffectLib API - Provided by the EffectLib plugin at runtime
    implementation("com.elmakers.mine.bukkit:EffectLib:10.2")
    // BUG FIX: Add the PacketEvents API as a transitive dependency for LibsDisguises.
    compileOnly("com.github.retrooper:packetevents-spigot:2.9.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}