import dev.slne.surf.api.gradle.util.registerRequired
import dev.slne.surf.api.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.protect.paper.PaperMain")
    foliaSupported(true)
    generateLibraryLoader(true)

    authors.addAll("Ammo", "twisti", "red")

    serverDependencies {
        registerRequired("surf-transaction-paper")
        registerRequired("WorldGuard")
        registerSoft("surf-hud-paper")
    }
}

dependencies {
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit) {
        isTransitive = true
        exclude("com.google.guava", "guava")
        exclude("com.google.code.gson", "gson")
        exclude("it.unimi.dsi", "fastutil")
    }
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:+")
    paperLibrary("pl.allegro.finance:tradukisto:4.3.3")
    compileOnly("dev.slne.surf.hud:surf-hud-api:1.0.1")
}

group = "dev.slne.surf.protect"
version = findProperty("version") as String
