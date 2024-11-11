package net.voxelpi.varp.loader

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.loader.serializer.PathSerializer
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
import kotlin.io.path.writeText
import kotlin.reflect.KClass

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
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        registerTypeAdapter(Path::class.java, PathSerializer(path))
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

    private fun loadRepositories(): Result<Unit> {
        repositories.clear()

        return runCatching {
            val repositoriesConfig = JsonParser.parseReader(path.resolve(REPOSITORIES_FILE).bufferedReader())
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
                @Suppress("UNCHECKED_CAST")
                val type = repositoryTypes[typeId.asString] as RepositoryType<Repository, RepositoryConfig>?
                if (type == null) {
                    logger.warn("Unable to load repository \"$id\": Unknown repository type \"${typeId.asString}\"")
                    continue
                }

                // Load the repository config.
                val config = try {
                    deserializeRepositoryConfig(repositoryConfig["config"], type.configType)
                } catch (exception: Exception) {
                    logger.warn("Unable to load repository \"$id\": Unable to load repository type config.", exception)
                    continue
                }

                // Create repository.
                val repositoryResult: Result<Repository> = type.create(id, config)
                if (repositoryResult.isFailure) {
                    logger.warn("Unable to create repository \"$id\".", repositoryResult.exceptionOrNull())
                    continue
                }
                val repository = repositoryResult.getOrThrow()
                repositories[repository.id] = repository
            }
        }
    }

    private fun saveRepositories(): Result<Unit> {
        return runCatching {
            val repositoriesJson = JsonObject()
            for (repository in repositories.values.sortedBy(Repository::id)) {
                val repositoryJson = JsonObject()

                // Add the repository type.
                repositoryJson.addProperty("type", repository.type.id)

                // Add the repository config.
                repositoryJson.add("config", serializeRepositoryConfig(repository.config))

                // Add to serialized repositories.
                repositoriesJson.add(repository.id, repositoryJson)
            }

            // Save the repositories file.
            path.resolve(REPOSITORIES_FILE).writeText(repositoriesJson.toString())
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

    private fun saveTree(): Result<Unit> {
        return runCatching {
            val treeJson = JsonObject()

            // Store mounts.
            val mountsJson = JsonArray()
            for (mount in compositor.mounts()) {
                val mountJson = JsonObject()
                mountJson.addProperty("location", mount.location.toString())
                mountJson.addProperty("repository", mount.repository.id)
                mountsJson.add(mountJson)
            }
            treeJson.add("mounts", mountsJson)

            // Save the tree file.
            path.resolve(TREE_FILE).writeText(treeJson.toString())
        }
    }

    private fun serializeRepositoryConfig(config: RepositoryConfig): JsonElement {
        // If the config is a kotlin object, return an empty json object.
        if (config::class.objectInstance != null || config::class.isCompanion) {
            return JsonObject()
        }

        // Serialize the config using gson.
        return gson.toJsonTree(config)
    }

    private fun deserializeRepositoryConfig(json: JsonElement, type: KClass<RepositoryConfig>): RepositoryConfig {
        require(json is JsonObject) { "Repository config must be a json object" }

        // Return the kotlin object instance if type is a kotlin object.
        if (type.objectInstance != null) {
            return type.objectInstance!!
        }

        // Deserialize the config using gson.
        return gson.fromJson(json, type.java)
    }

    private fun repositoryPath(id: String): Path {
        return repositoriesPath.resolve(id)
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
