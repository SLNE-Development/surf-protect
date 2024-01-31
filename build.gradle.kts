plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://packages.slne.dev/maven/p/surf/maven")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly(libs.io.papermc.paper.api)
    compileOnly(libs.dev.slne.surf.transaction.api)
    compileOnly(libs.dev.slne.surf.gui.bukkit)
    compileOnly(libs.dev.jorel.commandapi.bukkit.core)
    compileOnly(libs.com.sk89q.worldguard.worldguard.bukkit)
//    compileOnly("net.kyori:adventure-nbt:4.13.1") // TODO: 31.01.2024 12:38 - needed?
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
        options.compilerArgs.add("--parameters")
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}
