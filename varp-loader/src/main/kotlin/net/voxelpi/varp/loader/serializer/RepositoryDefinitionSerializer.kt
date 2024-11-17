package net.voxelpi.varp.loader.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.loader.model.RepositoryDefinition
import net.voxelpi.varp.warp.repository.RepositoryType
import java.lang.reflect.Type

internal class RepositoryDefinitionSerializer(
    private val typesProvider: () -> Map<String, RepositoryType<*, *>>,
) : JsonSerializer<RepositoryDefinition>, JsonDeserializer<RepositoryDefinition> {

    override fun serialize(repository: RepositoryDefinition, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val json = JsonObject()
        json.addProperty("id", repository.id)
        json.addProperty("type", repository.type.id)
        if (repository.config::class.objectInstance == null && !repository.config::class.isCompanion) {
            json.add("config", context.serialize(repository.config))
        }
        return json
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): RepositoryDefinition {
        check(json is JsonObject) { "Repository config must be a json object" }

        check(json.has("id")) { "Repository config must contain an id field" }
        val id = json.getAsJsonPrimitive("id").asString

        // Get the type id of the repository.
        val typeId = json["type"]
        check(typeId != null && typeId is JsonPrimitive) { "Repository config must contain a type field" }

        // Get the repository type.
        val type = typesProvider()[typeId.asString]
        check(type != null) { "Unknown repository type \"${typeId.asString}\"" }

        // Get the repository config.
        val config = if (type.configType.objectInstance != null) {
            type.configType.objectInstance!!
        } else {
            val configJson = json["config"]
            check(configJson != null && configJson is JsonObject) { "Repository config must contain a config field" }

            context.deserialize(configJson, type.configType.java)
        }

        return RepositoryDefinition(id, type, config)
    }
}
