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
import kotlin.io.path.writeText

public class VarpLoader internal constructor(
    public val path: Path,
    repositoryTypes: Collection<RepositoryType<*, *>>,
) {

    private val logger: Logger = LoggerFactory.getLogger(VarpLoader::class.java)

    public val repositoriesPath: Path = path.resolve("repositories")

    private val repositoryTypes: Map<String, RepositoryType<*, *>> = repositoryTypes.associateBy(RepositoryType<*, *>::id)

    private val repositories: MutableMap<String, Repository> = mutableMapOf()

    public val compositor: Compositor = Compositor.empty("main")

    public val tree: Tree
        get() = compositor.tree

    private val gson = GsonBuilder().apply {
        setPrettyPrinting()
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        varpSerializers()
        registerTypeAdapter(Path::class.java, PathSerializer(path))
        registerTypeAdapter(RepositoryDefinition::class.java, RepositoryDefinitionSerializer { this@VarpLoader.repositoryTypes })
    }.create()

    init {
        path.createDirectories()
        repositoriesPath.createDirectories()

        runBlocking {
            compositor.activate()
        }
    }

    public fun repositories(): Collection<Repository> {
        return repositories.values
    }

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
        compositor.load().getOrElse { return Result.failure(it) }
        return Result.success(Unit)
    }

    public suspend fun save(): Result<Unit> {
        saveRepositories().onFailure { return Result.failure(it) }
        saveTree().onFailure { return Result.failure(it) }
        return Result.success(Unit)
    }

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

    public fun repositoryPath(id: String): Path {
        return repositoriesPath.resolve(id)
    }

    private fun loadRepositories(): Result<Unit> {
        repositories.clear()

        return runCatching {
            val repositoriesList = JsonParser.parseReader(path.resolve(REPOSITORIES_FILE).bufferedReader())
            if (repositoriesList !is JsonArray) {
                return Result.failure(Exception("Repositories list must be a json array"))
            }

            for (repositoryJson in repositoriesList) {
                val definition = try {
                    gson.fromJson(repositoryJson, RepositoryDefinition::class.java)
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

    private fun loadTree(): Result<Unit> {
        return runCatching {
            // Deserialize tree configuration.
            val treeConfig = gson.fromJson<TreeConfiguration>(path.resolve(TREE_FILE).bufferedReader(), TreeConfiguration::class.java)

            // Generate mount list.
            val mounts = mutableListOf<CompositorMount>()
            for (mountDefinition in treeConfig.mounts) {
                val repository = repositories[mountDefinition.repository]
                if (repository == null) {
                    logger.warn("Unable to load mount, unknown repository \"${mountDefinition.repository}\"")
                    continue
                }

                val mount = CompositorMount(mountDefinition.location, repository)
                mounts.add(mount)
            }

            // Update compositor.
            compositor.updateMounts(mounts)
        }
    }

    private fun saveTree(): Result<Unit> {
        return runCatching {
            val mountDefinitions = compositor.mounts().map { mount -> MountDefinition(mount.location, mount.repository.id) }
            val treeConfiguration = TreeConfiguration(mountDefinitions)

            // Save the tree file.
            path.resolve(TREE_FILE).writeText(gson.toJson(treeConfiguration))
        }
    }

    public class Builder internal constructor(
        public val path: Path,
        repositoryTypes: Collection<RepositoryType<*, *>>,
    ) {
        private val repositoryTypes: MutableMap<String, RepositoryType<*, *>> = repositoryTypes.associateBy(RepositoryType<*, *>::id).toMutableMap()

        public fun registerRepositoryType(repositoryType: RepositoryType<*, *>): Builder {
            repositoryTypes[repositoryType.id] = repositoryType
            return this
        }

        public fun build(): VarpLoader {
            return VarpLoader(path, repositoryTypes.values)
        }
    }

    public companion object {

        public fun builder(path: Path): Builder {
            return Builder(path, DEFAULT_TYPES)
        }

        public fun loader(path: Path, action: Builder.() -> Unit): VarpLoader {
            val builder = Builder(path, DEFAULT_TYPES)
            action(builder)
            return builder.build()
        }

        public fun emptyBuilder(path: Path): Builder {
            return Builder(path, emptyList())
        }

        public fun emptyLoader(path: Path, action: Builder.() -> Unit): VarpLoader {
            val builder = Builder(path, emptyList())
            action(builder)
            return builder.build()
        }

        private val DEFAULT_TYPES = listOf<RepositoryType<*, *>>(
            EphemeralRepositoryType,
            CompositorType,
        )

        private const val REPOSITORIES_FILE = "repositories.json"
        private const val TREE_FILE = "tree.json"
    }
}
