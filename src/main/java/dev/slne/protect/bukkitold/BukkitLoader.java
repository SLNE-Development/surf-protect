package dev.slne.protect.bukkitold;

import com.google.gson.Gson;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

@SuppressWarnings("UnstableApiUsage")
public class BukkitLoader implements PluginLoader {

  @Override
  @SuppressWarnings({"java:S1192", "unstable"})
  public void classloader(PluginClasspathBuilder classpathBuilder) {
    MavenLibraryResolver resolver = new MavenLibraryResolver();
    PluginLibraries pluginLibraries = load();

    pluginLibraries.asDependencies().forEach(resolver::addDependency);
    pluginLibraries.asRepositories().forEach(resolver::addRepository);

    classpathBuilder.addLibrary(resolver);
  }

  private PluginLibraries load() {
    try (var in = getClass().getResourceAsStream("/paper-libraries.json")) {
      if (in != null) {
        return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8),
            PluginLibraries.class);
      }

      return new PluginLibraries(Map.of(), List.of());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private record PluginLibraries(Map<String, String> repositories, List<String> dependencies) {

    public Stream<Dependency> asDependencies() {
      return dependencies.stream()
          .map(d -> new Dependency(new DefaultArtifact(d), null));
    }

    public Stream<RemoteRepository> asRepositories() {
      return repositories.entrySet().stream()
          .map(e -> new RemoteRepository.Builder(e.getKey(), "default", e.getValue()).build());
    }
  }
}
