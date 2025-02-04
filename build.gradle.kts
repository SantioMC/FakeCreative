plugins {
    id("java")
    id("de.eldoria.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "9.0.0-beta6"
    id("io.freefair.lombok") version "8.12"
}

group = "me.santio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.github.retrooper:packetevents-spigot:2.7.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

bukkit {
    name = "FakeCreative"
    version = "1.0"
    description = "Fake creative mode for creative servers"
    author = "Santio71"
    main = "me.santio.fakegmc.FakeCreative"

    foliaSupported = true
    apiVersion = "1.13"

    commands {
        register("fakecreative") {
            description = "Go into creative mode"
            permission = "fakegmc.command"
        }
    }
}
