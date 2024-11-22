package net.voxelpi.varp.loader

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.loader.model.MountDefinition
import net.voxelpi.varp.loader.model.RepositoryDefinition
import net.voxelpi.varp.loader.model.TreeConfiguration
import net.voxelpi.varp.loader.serializer.PathSerializer
import net.voxelpi.varp.loader.serializer.RepositoryDefinitionSerializer
import net.voxelpi.varp.serializer.gson.varpSerializers
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryConfig
import net.voxelpi.varp.warp.repository.RepositoryType
import net.voxelpi.varp.warp.repository.compositor.Compositor
import net.voxelpi.varp.warp.repository.compositor.CompositorMount
import net.voxelpi.varp.warp.repository.compositor.CompositorType
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralRepositoryType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Loads varp trees from disk.
 * The configuration consists of a repository list file that contains the definitions for all registered repositories,
 * as well as a tree configuration that configures the compositor.
 *
 * @property path The directory in which the loader should work.
 * @property repositoryTypes All repository types that the loader should use.
 */
public class VarpLoader internal constructor(
    public val path: Path,
    repositoryTypes: Collection<RepositoryType<*, *>>,
    private val defaultRepositories: Collection<RepositoryDefinition>,
    private val defaultMounts: Collection<MountDefinition>,
) {

    private val logger: Logger = LoggerFactory.getLogger(VarpLoader::class.java)

    /**
     * The directory in which the data of repositories is stored.
     * Note that not all repositories store information on disk.
     */
    public val repositoriesDataDirectory: Path = path.resolve("repositories")

    private val repositoryTypes: Map<String, RepositoryType<*, *>> = repositoryTypes.associateBy(RepositoryType<*, *>::id)

    private val repositories: MutableMap<String, Repository> = mutableMapOf()

    /**
     * The compositor that is generated by the loader.
     */
    public val compositor: Compositor = Compositor.empty("main")

    /**
     * The tree of the compositor.
     */
    public val tree: Tree
        get() = compositor.tree

    private val gson = GsonBuilder().apply {
        setPrettyPrinting()
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        varpSerializers()
        registerTypeHierarchyAdapter(Path::class.java, PathSerializer(repositoriesDataDirectory))
        registerTypeAdapter(RepositoryDefinition::class.java, RepositoryDefinitionSerializer { this@VarpLoader.repositoryTypes })
    }.create()

    init {
        path.createDirectories()
        repositoriesDataDirectory.createDirectories()

        runBlocking {
            compositor.activate()
        }
    }

    /**
     * Returns all currently loaded repositories.
     */
    public fun repositories(): Collection<Repository> {
        return repositories.values
    }

    /**
     * Returns the repository with the given [id],
     * or null if no repository with that id is currently loaded.
     */
    public fun repository(id: String): Repository? {
        return repositories[id]
    }

    /**
     * Returns the directory in which a repository with the given [id] stores its data.
     * It is not guaranteed, that that directory exists, as it may not be used by the repository.
     */
    public fun repositoryDataDirectory(id: String): Path {
        return repositoriesDataDirectory.resolve(id)
    }

    /**
     * Loads the varp tree.
     */
    public suspend fun load(): Result<Unit> {
        // Deactivate all existing repositories.
        coroutineScope {
            for (repository in repositories.values) {
                launch {
                    repository.deactivate()
                }
            }
        }

        loadRepositories().onFailure { return Result.failure(it) }

        coroutineScope {
            for (repository in repositories.values) {
                launch {
                    repository.activate()
                }
            }
        }

        loadTree().onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

    /**
     * Saves the varp tree.
     */
    public suspend fun save(): Result<Unit> {
        saveRepositories().onFailure { return Result.failure(it) }
        saveTree().onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

    /**
     * Deactivates the compositor and saves all configuration files.
     */
    public suspend fun cleanup(): Result<Unit> {
        // Deactivate all existing repositories.
        coroutineScope {
            for (repository in repositories.values) {
                launch {
                    repository.deactivate()
                }
            }
        }

        compositor.deactivate()

        return Result.success(Unit)
    }

    private fun loadRepositories(): Result<Unit> {
        repositories.clear()

        return runCatching {
            val repositoriesFile = path.resolve(REPOSITORIES_FILE)

            // Save default repositories if the repositories file doesn't exist.
            repositoriesFile.parent.createDirectories()
            if (!repositoriesFile.exists()) {
                repositoriesFile.writeText(gson.toJson(defaultRepositories.toList()))
            }

            // Load repositories for repositories file.
            val repositoriesList = JsonParser.parseReader(repositoriesFile.bufferedReader())
            if (repositoriesList !is JsonArray) {
                return Result.failure(Exception("Repositories list must be a json array"))
            }

            // Load definitions.
            for (repositoryJson in repositoriesList) {
                val definition = try {
                    gson.fromJson(repositoryJson, RepositoryDefinition::class.java)!!
                } catch (exception: Exception) {
                    logger.warn("Unable to load repository: ${exception.message}")
                    continue
                }

                if (definition.id.isBlank()) {
                    logger.warn("Unable to load repository \"${definition.id}\". Invalid repository id.")
                    continue
                }

                if (definition.id in repositories) {
                    logger.warn("Unable to load repository \"${definition.id}\". A repository with that id already exists.")
                    continue
                }

                // Create repository.
                @Suppress("UNCHECKED_CAST")
                val repositoryResult: Result<Repository> = (definition.type as RepositoryType<Repository, RepositoryConfig>).create(definition.id, definition.config)
                if (repositoryResult.isFailure) {
                    logger.warn("Unable to create repository \"${definition.id}\".", repositoryResult.exceptionOrNull())
                    continue
                }
                val repository = repositoryResult.getOrThrow()
                repositories[repository.id] = repository
            }
        }
    }

    private fun saveRepositories(): Result<Unit> {
        return runCatching {
            val definitions = repositories.values.map { RepositoryDefinition(it.id, it.type, it.config) }
            path.resolve(REPOSITORIES_FILE).writeText(gson.toJson(definitions))
        }
    }

    private suspend fun loadTree(): Result<Unit> {
        return runCatching {
            val treeFile = path.resolve(TREE_FILE)

            // Save default repositories if the repositories file doesn't exist.
            treeFile.parent.createDirectories()
            if (!treeFile.exists()) {
                treeFile.writeText(gson.toJson(TreeConfiguration(defaultMounts.toList())))
            }

            // Deserialize tree configuration.
            val treeConfig = gson.fromJson<TreeConfiguration>(treeFile.bufferedReader(), TreeConfiguration::class.java)

            // Generate mount list.
            val mounts = mutableListOf<CompositorMount>()
            for (mountDefinition in treeConfig.mounts) {
                val repository = repositories[mountDefinition.repository]
                if (repository == null) {
                    logger.warn("Unable to load mount, unknown repository \"${mountDefinition.repository}\"")
                    continue
                }

                val mount = CompositorMount(mountDefinition.path, repository, mountDefinition.sourcePath)
                mounts.add(mount)
            }

            // Update compositor.
            compositor.modifyMounts(mounts)
        }
    }

    private fun saveTree(): Result<Unit> {
        return runCatching {
            val mountDefinitions = compositor.mounts().map { mount -> MountDefinition(mount.path, mount.repository.id, mount.sourcePath) }
            val treeConfiguration = TreeConfiguration(mountDefinitions)

            // Save the tree file.
            path.resolve(TREE_FILE).writeText(gson.toJson(treeConfiguration))
        }
    }

    /**
     * Loader builder class.
     * @property path The base path from where the tree config and repository list are stored.
     */
    public class Builder internal constructor(
        public val path: Path,
        repositoryTypes: Collection<RepositoryType<*, *>>,
    ) {
        private val repositoryTypes: MutableMap<String, RepositoryType<*, *>> = repositoryTypes.associateBy(RepositoryType<*, *>::id).toMutableMap()
        private val defaultRepositories: MutableMap<String, RepositoryDefinition> = mutableMapOf()
        private val defaultMounts: MutableMap<NodeParentPath, MountDefinition> = mutableMapOf()

        /**
         * Registers the given [repositoryType] to the loader.
         */
        public fun registerRepositoryType(repositoryType: RepositoryType<*, *>): Builder {
            repositoryTypes[repositoryType.id] = repositoryType
            return this
        }

        /**
         * Registers the standard repository types to the loader.
         * The standard repository types are currently the compositor type and the ephemeral type.
         */
        public fun registerStandardTypes(): Builder {
            repositoryTypes.putAll(STANDARD_TYPES.associateBy { it.id })
            return this
        }

        /**
         * Adds a new default repository with default mounts.
         */
        public fun <C : RepositoryConfig> addDefaultRepository(id: String, type: RepositoryType<*, C>, config: C, mounts: Collection<Pair<NodeParentPath, NodeParentPath>>): Builder {
            defaultRepositories[id] = RepositoryDefinition(id, type, config)
            for ((path, repositoryPath) in mounts) {
                defaultMounts[path] = MountDefinition(path, id, repositoryPath)
            }
            return this
        }

        /**
         * Creates the loader from this builder.
         */
        public fun build(): VarpLoader {
            return VarpLoader(path, repositoryTypes.values, defaultRepositories.values, defaultMounts.values)
        }
    }

    public companion object {

        /**
         * Creates a new loader builder with the default repository types already registered.
         */
        public fun builder(path: Path): Builder {
            return Builder(path, STANDARD_TYPES)
        }

        /**
         * Creates a new loader with the default types already registered.
         */
        public fun loader(path: Path, action: Builder.() -> Unit): VarpLoader {
            val builder = Builder(path, STANDARD_TYPES)
            action(builder)
            return builder.build()
        }

        /**
         * Creates a new loader builder without any preregistered repository types.
         */
        public fun emptyBuilder(path: Path): Builder {
            return Builder(path, emptyList())
        }

        /**
         * Creates a new loader without any preregistered repository types.
         */
        public fun emptyLoader(path: Path, action: Builder.() -> Unit): VarpLoader {
            val builder = Builder(path, emptyList())
            action(builder)
            return builder.build()
        }

        /**
         * The standard repository types.
         */
        private val STANDARD_TYPES = listOf<RepositoryType<*, *>>(
            EphemeralRepositoryType,
            CompositorType,
        )

        private const val REPOSITORIES_FILE = "repositories.json"
        private const val TREE_FILE = "tree.json"
    }
}
