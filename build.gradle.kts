import dev.slne.surf.surfapi.gradle.util.registerRequired
import dev.slne.surf.surfapi.gradle.util.slnePrivate

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin") version "1.21.7+"
}

repositories {
    mavenLocal()
    slnePrivate()
}

surfPaperPluginApi {
    mainClass("dev.slne.protect.paper.PaperMain")
    foliaSupported(false)
    generateLibraryLoader(false)

    serverDependencies {
        registerRequired("surf-transaction-paper")
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
    implementation(libs.net.wesjd.anvilgui)
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "spigot"
    }
}

group = "dev.slne.surf.protect"
version = "1.21.7-1.0.4-SNAPSHOT"

