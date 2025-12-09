package net.voxelpi.varp.environment

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.environment.model.RepositoryDefinition
import net.voxelpi.varp.environment.serializer.PathSerializer
import net.voxelpi.varp.environment.serializer.RepositoryDefinitionSerializer
import net.voxelpi.varp.repository.RepositoryType
import net.voxelpi.varp.repository.compositor.CompositorType
import net.voxelpi.varp.repository.ephemeral.EphemeralRepositoryType
import net.voxelpi.varp.serializer.gson.varpSerializers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.bufferedReader
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists
import kotlin.io.path.writeText

public class VarpEnvironmentLoader internal constructor(
    repositoryTypes: Collection<RepositoryType<*, *>>,
) {
    private val logger: Logger = LoggerFactory.getLogger(VarpEnvironmentLoader::class.java)

    private val repositoryTypes: Map<String, RepositoryType<*, *>> = repositoryTypes.associateBy(RepositoryType<*, *>::id)

    public fun load(
        environmentFilePath: Path,
    ): Result<EnvironmentDefinition?> = runCatching {
        // Check if environment file exists.
        if (environmentFilePath.notExists()) {
            return@runCatching null
        }

        // Check that the path points to a regular file.
        if (!environmentFilePath.isRegularFile()) {
            throw IllegalStateException("Environment file '${environmentFilePath.absolute()}' is not a regular file")
        }

        // Parse the environment definition file.
        val serializedDefinition = JsonParser.parseReader(environmentFilePath.bufferedReader())
        if (serializedDefinition !is JsonObject) {
            throw IllegalStateException("Invalid environment file '${environmentFilePath.absolute()}'")
        }

        // Deserialize the environment definition.
        return@runCatching loadFromJson(serializedDefinition, environmentFilePath.parent).getOrThrow()
    }

    public fun loadFromJson(
        json: JsonObject,
        workingDirectory: Path,
    ): Result<EnvironmentDefinition?> = runCatching {
        val gson = gsonBuilder(workingDirectory).create()
        val version = json["version"].asString
        val definition = gson.fromJson(json, EnvironmentDefinition::class.java)
        return@runCatching definition
    }

    public fun save(
        environment: EnvironmentDefinition,
        environmentFilePath: Path,
    ): Result<Unit> = runCatching {
        val gson = gsonBuilder(environmentFilePath.parent).create()
        val serializedDefinition = gson.toJsonTree(environment) as JsonObject
        serializedDefinition.addProperty("version", 1)
        environmentFilePath.writeText(gson.toJson(environment))
    }

    public fun saveToJson(
        environment: EnvironmentDefinition,
        workingDirectory: Path,
    ): Result<JsonObject> = runCatching {
        val gson = gsonBuilder(workingDirectory).create()
        val serializedDefinition = gson.toJsonTree(environment) as JsonObject
        serializedDefinition.addProperty("version", 1)
        return@runCatching serializedDefinition
    }

    public fun saveToString(
        environment: EnvironmentDefinition,
        workingDirectory: Path,
    ): Result<String> = runCatching {
        val gson = gsonBuilder(workingDirectory).create()
        val serializedDefinition = gson.toJsonTree(environment) as JsonObject
        serializedDefinition.addProperty("version", 1)
        return@runCatching gson.toJson(serializedDefinition)
    }

    private fun gsonBuilder(repositoriesDirectory: Path): GsonBuilder {
        return GsonBuilder().apply {
            setPrettyPrinting()
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            varpSerializers()
            registerTypeHierarchyAdapter(Path::class.java, PathSerializer(repositoriesDirectory))
            registerTypeAdapter(RepositoryDefinition::class.java, RepositoryDefinitionSerializer { repositoryTypes })
        }
    }

    public companion object {
        /**
         * The standard repository types.
         */
        private val STANDARD_TYPES = listOf<RepositoryType<*, *>>(
            EphemeralRepositoryType,
            CompositorType,
        )

        /**
         * Creates a new loader with the default repository types already registered.
         */
        public fun withStandardTypes(types: Collection<RepositoryType<*, *>>): VarpEnvironmentLoader {
            return VarpEnvironmentLoader(STANDARD_TYPES + types)
        }

        /**
         * Creates a new loader builder without any preregistered repository types.
         */
        public fun withoutStandardTypes(types: Collection<RepositoryType<*, *>>): VarpEnvironmentLoader {
            return VarpEnvironmentLoader(types)
        }
    }
}
