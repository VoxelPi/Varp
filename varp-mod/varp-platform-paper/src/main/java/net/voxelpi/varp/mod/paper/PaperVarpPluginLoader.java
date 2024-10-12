package net.voxelpi.varp.mod.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

/**
 * Loader for the varp paper plugin.
 * Because the kotlin stdlib is loaded by the loader, the loader itself can't be written in kotlin.
 */
@SuppressWarnings("UnstableApiUsage")
public class PaperVarpPluginLoader implements PluginLoader {

    private static final String CLOUD_VERSION = "2.0.0";
    private static final String CLOUD_MINECRAFT_VERSION = "2.0.0-beta.10";
    private static final String KOTLIN_VERSION = "2.0.21";

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/repositories/snapshots").build());

        // Load dependencies
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-stdlib", KOTLIN_VERSION);
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-reflect", KOTLIN_VERSION);

        addDependency(resolver, "org.incendo:cloud-core", CLOUD_VERSION);
        addDependency(resolver, "org.incendo:cloud-kotlin-extensions", CLOUD_VERSION);
        addDependency(resolver, "org.incendo:cloud-minecraft-extras", CLOUD_MINECRAFT_VERSION);
        addDependency(resolver, "org.incendo:cloud-paper", CLOUD_MINECRAFT_VERSION);

        classpathBuilder.addLibrary(resolver);
    }

    private static void addDependency(MavenLibraryResolver resolver, String dependency, String version) {
        resolver.addDependency(new Dependency(new DefaultArtifact(dependency + ":" + version), null));
    }
}
