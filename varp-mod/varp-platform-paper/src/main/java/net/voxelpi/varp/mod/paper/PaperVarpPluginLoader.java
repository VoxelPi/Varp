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

    private static final String ADVENTURE_VERSION = "4.25.0";
    private static final String CLOUD_VERSION = "2.0.0";
    private static final String CLOUD_MINECRAFT_VERSION = "2.0.0-beta.13";
    private static final String CONFIGURATE_VERSION = "4.2.0";
    private static final String KOTLIN_VERSION = "2.2.21";
    private static final String MOONSHINE_VERSION = "2.0.4";

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/repositories/snapshots").build());

        // Load dependencies
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-stdlib", KOTLIN_VERSION);
        addDependency(resolver, "org.jetbrains.kotlin:kotlin-reflect", KOTLIN_VERSION);

        addDependency(resolver, "org.incendo:cloud-core", CLOUD_VERSION);
        addDependency(resolver, "org.incendo:cloud-kotlin-extensions", CLOUD_VERSION);
        addDependency(resolver, "org.incendo:cloud-minecraft-extras", CLOUD_MINECRAFT_VERSION);
        addDependency(resolver, "org.incendo:cloud-paper", CLOUD_MINECRAFT_VERSION);

        addDependency(resolver, "org.spongepowered:configurate-core", CONFIGURATE_VERSION);
        addDependency(resolver, "org.spongepowered:configurate-extra-kotlin", CONFIGURATE_VERSION);
        addDependency(resolver, "org.spongepowered:configurate-gson", CONFIGURATE_VERSION);
        addDependency(resolver, "org.spongepowered:configurate-hocon", CONFIGURATE_VERSION);
        addDependency(resolver, "org.spongepowered:configurate-xml", CONFIGURATE_VERSION);
        addDependency(resolver, "org.spongepowered:configurate-yaml", CONFIGURATE_VERSION);
        addDependency(resolver, "net.kyori:adventure-serializer-configurate4", ADVENTURE_VERSION);

        addDependency(resolver, "net.kyori.moonshine:moonshine-core", MOONSHINE_VERSION);
        addDependency(resolver, "net.kyori.moonshine:moonshine-standard", MOONSHINE_VERSION);

        classpathBuilder.addLibrary(resolver);
    }

    private static void addDependency(MavenLibraryResolver resolver, String dependency, String version) {
        resolver.addDependency(new Dependency(new DefaultArtifact(dependency + ":" + version), null));
    }
}
