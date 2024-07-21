plugins {
    `java-library`
    `maven-publish`

    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

repositories {
    gradlePluginPortal()
    maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.dev.slne.surf.transaction.api)
    compileOnly(libs.dev.jorel.commandapi.bukkit.core)
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
    compileOnly(libs.dev.slne.surf.surf.api.bukkit.api)

    implementation(libs.net.wesjd.anvilgui)
    implementation(libs.com.github.stefvanschie.inventoryframework)
}

group = "dev.slne.surf"
version = "1.21-1.0.0-SNAPSHOT"
description = "surf-protect"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
}

publishing {
    repositories {
        maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
    }

    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
    shadowJar {
        relocate("com.github.stefvanschie.inventoryframework", "dev.slne.protect.inventoryframework")
        relocate("net.wesjd.anvilgui", "dev.slne.protect.anvilgui")
        manifest { attributes["paperweight-mappings-namespace"] = "spigot" }
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

bukkit {
    main = "dev.slne.protect.bukkit.BukkitMain"
    apiVersion = "1.21"
    authors = listOf("ammo", "SLNE Development")
    depend = arrayListOf("CommandAPI", "surf-bukkit-api", "surf-transaction-bukkit", "WorldGuard")
}
