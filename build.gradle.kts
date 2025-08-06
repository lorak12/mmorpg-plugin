import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.nakii.mmorpg"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Paper's official repository
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { name = "spigot-repo"; url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { name = "md5-repo"; url = uri("https://repo.md-5.net/content/groups/public/") }
    maven { name = "elmakers-repo"; url = uri("https://maven.elmakers.com/repository/") }
}

dependencies {
    // Switched to paper-api
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    implementation("org.xerial:sqlite-jdbc:3.36.0.3")

    compileOnly(group = "me.libraryaddict.disguises", name = "libsdisguises", version = "11.0.0")
    compileOnly("com.elmakers.mine.bukkit:EffectLib:9.3")

    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    relocate("net.kyori", "org.nakii.mmorpg.libs.kyori")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}