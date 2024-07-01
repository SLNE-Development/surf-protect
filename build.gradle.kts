import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`
    `maven-publish`

    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("io.github.goooler.shadow") version "8.1.7"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    gradlePluginPortal()
    maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    maven("https://repo.slne.dev/repository/maven-snapshots/") { name = "maven-snapshots" }
}

dependencies {
    paperweight.paperDevBundle(libs.io.papermc.paper.api.get().version)
    compileOnly(libs.dev.slne.surf.transaction.api)
    compileOnly(libs.dev.jorel.commandapi.bukkit.core)
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
    compileOnly(libs.dev.slne.surf.surf.api.bukkit.api)

    api(libs.net.wesjd.anvilgui)
    api(libs.com.github.stefvanschie.inventoryframework)
}

group = "dev.slne.surf"
version = "1.21-1.0.0-SNAPSHOT"
description = "surf-protect"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

//    withJavadocJar()
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
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
    }
    assemble {
        dependsOn(reobfJar)
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
        registerDepend("CommandAPI")
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
