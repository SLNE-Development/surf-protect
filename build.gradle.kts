import dev.slne.surf.surfapi.gradle.util.registerRequired
import dev.slne.surf.surfapi.gradle.util.slnePrivate

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin") version "1.21.7+"
}

repositories {
    slnePrivate()
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.protect.paper.PaperMain")
    foliaSupported(true)
    generateLibraryLoader(true)

    serverDependencies {
        registerRequired("surf-transaction-bukkit")
        registerRequired("WorldGuard")
    }
}

dependencies {
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit) {
        isTransitive = true
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
    }

    compileOnly(libs.dev.slne.surf.transaction.api)
    paperLibrary("pl.allegro.finance:tradukisto:4.1.0")
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
}

group = "dev.slne.surf.protect"
version = "1.21.7-2.0.0"

