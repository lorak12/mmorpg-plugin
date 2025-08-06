plugins {
    java
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
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // We will use 'compileOnly' for everything. The server provides these.
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(group = "me.libraryaddict.disguises", name = "libsdisguises", version = "11.0.0")

    // --- ADDED: ParticleSFX Dependency ---
    // 'compileOnly' means we need it to write our code, but we do NOT bundle it.
    compileOnly("io.github.hmzel:particlesfx:1.21.7")
}

java {
    // Set our project's Java version to 21, as required by Paper 1.21.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
