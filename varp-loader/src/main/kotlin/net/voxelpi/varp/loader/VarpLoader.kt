package net.voxelpi.varp.loader

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.voxelpi.varp.Varp
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.repository.TreeRepository
import net.voxelpi.varp.warp.repository.TreeRepositoryConfig
import net.voxelpi.varp.warp.repository.TreeRepositoryType
import net.voxelpi.varp.warp.repository.compositor.TreeCompositor
import net.voxelpi.varp.warp.repository.compositor.TreeCompositorType
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralTreeRepositoryConfig
import net.voxelpi.varp.warp.repository.ephemeral.EphemeralTreeRepositoryType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.createDirectories

public class VarpLoader internal constructor(
    public val path: Path,
    repositoryTypes: Collection<TreeRepositoryType<*, *>>,
) {

    private val logger: Logger = LoggerFactory.getLogger(VarpLoader::class.java)

    public val repositoriesPath: Path = path.resolve("repositories")

    private val repositoryTypes: Map<String, TreeRepositoryType<*, *>> = repositoryTypes.associateBy(TreeRepositoryType<*, *>::id)

    private val repositories: MutableMap<String, TreeRepository> = mutableMapOf()

    public var compositor: TreeCompositor = TreeCompositor.simple("main", EphemeralTreeRepositoryType.createRepository("default", EphemeralTreeRepositoryConfig))
        private set

    public var tree: Tree = Varp.createTree(compositor)
        private set

    private val gson = GsonBuilder().apply {
        setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    }.create()

    init {
        path.createDirectories()
        repositoriesPath.createDirectories()
    }

    public fun repositories(): Collection<TreeRepository> {
        return repositories.values
    }

    public fun load(): Result<Unit> {
        loadRepositories().getOrElse { return Result.failure(it) }

        tree = Varp.createTree(compositor)

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

                // Load the repository config.
                val config = runCatching {
                    gson.fromJson(repositoryConfig, type.configType)
                }.getOrNull()
                if (config == null) {
                    logger.warn("Unable to load repository \"$id\": Unable to load repository type config")
                    continue
                }

                // Create the repository.
                @Suppress("UNCHECKED_CAST")
                val repository = (type as TreeRepositoryType<TreeRepository, TreeRepositoryConfig>).createRepository(id, config)
                repositories[repository.id] = repository
            }
        }
    }

    public class Builder internal constructor(
        public val path: Path,
        repositoryTypes: Collection<TreeRepositoryType<*, *>>,
    ) {
        private val repositoryTypes: MutableMap<String, TreeRepositoryType<*, *>> = repositoryTypes.associateBy(TreeRepositoryType<*, *>::id).toMutableMap()

        public fun registerRepositoryType(repositoryType: TreeRepositoryType<*, *>): Builder {
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

        private val DEFAULT_TYPES = listOf<TreeRepositoryType<*, *>>(
            EphemeralTreeRepositoryType,
            TreeCompositorType,
        )

        private const val REPOSITORIES_FILE = "repositories.json"
        private const val TREE_FILE = "tree.json"
    }
}
