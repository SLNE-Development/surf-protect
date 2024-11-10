import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`
    `maven-publish`

    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.dev.slne.surf.transaction.api)
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
    compileOnly(libs.dev.slne.surf.surf.api.bukkit.api)

    implementation(libs.net.wesjd.anvilgui)
}

configurations.all {
    resolutionStrategy {
        force("it.unimi.dsi:fastutil:8.5.12") // TODO: WTF
    }
}


group = "dev.slne.surf"
version = "1.21-1.0.1-SNAPSHOT"
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

paper {
    main = "dev.slne.protect.bukkit.BukkitMain"
    loader = "dev.slne.protect.bukkit.BukkitLoader"
    apiVersion = "1.21"
    authors = listOf("ammo", "SLNE Development")

    serverDependencies {
        registerDepend("surf-bukkit-api")
        registerDepend("surf-transaction-bukkit")
        registerDepend("WorldGuard")
    }
}

fun NamedDomainObjectContainerScope<PaperPluginDescription.DependencyDefinition>.registerDepend(
    name: String,
    required: Boolean = true,
    load: PaperPluginDescription.RelativeLoadOrder = PaperPluginDescription.RelativeLoadOrder.BEFORE,
    joinClasspath: Boolean = true
) = register(name) {
    this.required = required
    this.load = load
    this.joinClasspath = joinClasspath
}
