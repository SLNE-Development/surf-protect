import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`
    `maven-publish`

    id("org.hibernate.build.maven-repo-auth") version "3.0.4"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
    maven("https://packages.slne.dev/maven/p/surf/maven") { name = "space-maven" }
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.dev.slne.surf.transaction.api)
    compileOnly(libs.dev.slne.surf.gui.bukkit)
    compileOnly(libs.dev.jorel.commandapi.bukkit.core)
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
    compileOnly(libs.dev.slne.surf.surf.api.bukkit.api)
}

group = "dev.slne.surf"
version = "2.1.0-SNAPSHOT"
description = "surf-protect"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "space-maven-production"
            url = uri(System.getenv("REPOSITORY_URL") ?: "https://packages.slne.dev/maven/p/surf/maven")
            credentials {
                username = System.getenv("JB_SPACE_CLIENT_ID")
                password = System.getenv("JB_SPACE_CLIENT_SECRET")
            }
        }
    }

    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks {
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
    apiVersion = "1.20"
    authors = listOf("ammo", "SLNE Development")

    serverDependencies {
        registerDepend("CommandAPI")
        registerDepend("SurfBukkitAPI")
        registerDepend("surf-transaction-bukkit")
        registerDepend("WorldGuard")
        registerDepend("surf-gui-bukkit")
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
