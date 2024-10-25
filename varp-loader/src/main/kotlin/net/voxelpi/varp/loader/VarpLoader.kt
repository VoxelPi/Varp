package net.voxelpi.varp.loader

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.Varp
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.RepositoryConfig
import net.voxelpi.varp.warp.repository.RepositoryTypeData
import net.voxelpi.varp.warp.repository.compositor.Compositor
import net.voxelpi.varp.warp.repository.compositor.CompositorMount
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories
import kotlin.reflect.KClass

public class VarpLoader internal constructor(
    public val path: Path,
    repositoryTypes: Collection<RepositoryTypeData>,
) {

    private val logger: Logger = LoggerFactory.getLogger(VarpLoader::class.java)

    public val repositoriesPath: Path = path.resolve("repositories")

    private val repositoryTypes: Map<String, RepositoryTypeData> = repositoryTypes.associateBy(RepositoryTypeData::id)

    private val repositories: MutableMap<String, Repository> = mutableMapOf()

    public var compositor: Compositor = Compositor.empty("main")
        private set

    public var tree: Tree = Varp.createTree(compositor)
        private set

    private val gson = GsonBuilder().apply {
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
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

    private fun loadRepositories(): Result<Unit> {
        repositories.clear()

        return runCatching {
            val repositoriesConfig = JsonParser.parseReader(path.resolve("repositories.json").bufferedReader())
            if (repositoriesConfig !is JsonObject) {
                return Result.failure(Exception("Repositories config must be a json object"))
            }

            for ((id, repositoryConfig) in repositoriesConfig.entrySet()) {
                // Check that the repository id is valid.
                if (id.isEmpty()) {
                    logger.warn("Unable to load repository \"$id\": Invalid id")
                    continue
                }

                // Check that the repository config is valid.
                if (repositoryConfig !is JsonObject) {
                    logger.warn("Unable to load repository \"$id\": Repository config must be a json object")
                    continue
                }

                // Get the type id of the repository.
                val typeId = repositoryConfig["type"]
                if (typeId == null || typeId !is JsonPrimitive) {
                    logger.warn("Unable to load repository \"$id\": Missing valid type property")
                    continue
                }

                // Get the repository type.
                val type = repositoryTypes[typeId.asString]
                if (type == null) {
                    logger.warn("Unable to load repository \"$id\": Unknown repository type \"${typeId.asString}\"")
                    continue
                }

                val repositoryResult: Result<Repository> = when (type) {
                    is RepositoryTypeData.WithConfig<*> -> {
                        // Load the repository config.
                        val configResult = runCatching {
                            gson.fromJson<RepositoryConfig>(repositoryConfig["config"], type.configType.java)
                        }
                        if (configResult.isFailure) {
                            logger.warn("Unable to load repository \"$id\": Unable to load repository type config.", configResult.exceptionOrNull())
                            continue
                        }
                        val config = configResult.getOrThrow()
                        if (config == null) {
                            logger.warn("Unable to load repository \"$id\": Unable to load repository type config.")
                            continue
                        }

                        @Suppress("UNCHECKED_CAST")
                        (type.generator as (id: String, config: RepositoryConfig) -> Result<Repository>).invoke(id, config)
                    }
                    is RepositoryTypeData.NoArgs -> {
                        type.generator.invoke(id)
                    }
                    is RepositoryTypeData.WithPath -> {
                        type.generator.invoke(id, repositoryPath(id))
                    }
                    is RepositoryTypeData.WithPathConfig<*> -> {
                        // Load the repository config.
                        val configResult = runCatching {
                            gson.fromJson<RepositoryConfig>(repositoryConfig["config"], type.configType.java)
                        }
                        if (configResult.isFailure) {
                            logger.warn("Unable to load repository \"$id\": Unable to load repository type config.", configResult.exceptionOrNull())
                            continue
                        }
                        val config = configResult.getOrThrow()
                        if (config == null) {
                            logger.warn("Unable to load repository \"$id\": Unable to load repository type config.")
                            continue
                        }

                        @Suppress("UNCHECKED_CAST")
                        (type.generator as (id: String, path: Path, config: RepositoryConfig) -> Result<Repository>).invoke(id, repositoryPath(id), config)
                    }
                }
                if (repositoryResult.isFailure) {
                    logger.warn("Unable to create repository \"$id\".", repositoryResult.exceptionOrNull())
                    continue
                }
                val repository = repositoryResult.getOrThrow()
                repositories[repository.id] = repository
            }
        }
    }

    private fun loadTree(): Result<Unit> {
        return runCatching {
            val mounts = mutableListOf<CompositorMount>()

            val treeConfig = JsonParser.parseReader(path.resolve(TREE_FILE).bufferedReader())
            if (treeConfig !is JsonObject) {
                return Result.failure(Exception("Tree config must be a json object"))
            }

            val mountsConfig = treeConfig["mounts"]
            if (mountsConfig !is JsonArray) {
                return Result.failure(Exception("Mounts config must be a json array"))
            }

            for (mountConfig in mountsConfig) {
                if (mountConfig !is JsonObject) {
                    logger.warn("Unable to load mount, invalid format")
                    continue
                }

                val locationResult = NodeParentPath.parse(mountConfig["location"].asString)
                if (locationResult.isFailure) {
                    logger.warn("Unable to load mount, invalid location \"${(mountConfig["location"])}\"")
                    continue
                }
                val location = locationResult.getOrThrow()

                val repositoryId = mountConfig["repository"].asString
                val repository = repositories[repositoryId]
                if (repository == null) {
                    logger.warn("Unable to load mount, unknown repository \"$repositoryId\"")
                    continue
                }

                val mount = CompositorMount(location, repository)
                mounts.add(mount)
            }

            compositor.updateMounts(mounts)
        }
    }

    private fun repositoryPath(id: String): Path {
        return repositoriesPath.resolve(id)
    }

    public class Builder internal constructor(
        public val path: Path,
        repositoryTypes: Collection<KClass<out Repository>>,
    ) {
        private val repositoryTypes: MutableMap<String, RepositoryTypeData> = mutableMapOf() // repositoryTypes.associateBy(RepositoryType<*, *>::id).toMutableMap()

        init {
            repositoryTypes.forEach(this::registerRepositoryType)
        }

        public fun registerRepositoryType(repositoryType: KClass<out Repository>): Builder {
            val data = RepositoryTypeData.Companion.fromClass(repositoryType).getOrThrow()
            require(data != null) { "Repository is not marked as type" }
            repositoryTypes[data.id] = data
            return this
        }

        public inline fun <reified T : Repository> registerRepositoryType(): Builder {
            return registerRepositoryType(T::class)
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

        private val DEFAULT_TYPES = listOf<KClass<out Repository>>(
            EphemeralRepository::class,
            Compositor::class,
        )

        private const val REPOSITORIES_FILE = "repositories.json"
        private const val TREE_FILE = "tree.json"
    }
}
