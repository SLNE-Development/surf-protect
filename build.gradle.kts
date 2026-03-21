import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin") version "1.21.11+"
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.protect.paper.PaperMain")
    foliaSupported(true)
    generateLibraryLoader(true)

    authors.addAll("Ammo", "twisti", "red")

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
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:1.21.11-3.0.1")
    paperLibrary("pl.allegro.finance:tradukisto:4.1.0")
}

group = "dev.slne.surf.protect"
version = "1.21.11-3.0.1"

