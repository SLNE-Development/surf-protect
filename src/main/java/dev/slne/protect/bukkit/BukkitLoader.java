package dev.slne.protect.bukkit;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

public class BukkitLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver mavenResolver = new MavenLibraryResolver();

        // Repositories
        mavenResolver.addRepository(
                new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        mavenResolver.addRepository(
                new RemoteRepository.Builder("sonatype-oss-snapshots1", "default",
                        "https://s01.oss.sonatype.org/content/repositories/snapshots/").build());

        // Dependencies
        mavenResolver.addDependency(
                new Dependency(new DefaultArtifact("dev.jorel:commandapi-bukkit-shade:9.0.1"), null));
        mavenResolver.addDependency(
                new Dependency(new DefaultArtifact("net.kyori:adventure-nbt:4.13.1"), null));

        // Resolve
        classpathBuilder.addLibrary(mavenResolver);
    }

}
