package net.voxelpi.varp.loader.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.key.Key
import net.voxelpi.varp.repository.Storage
import java.lang.reflect.Type

internal class StorageSerializer(
    private val storageProvider: () -> Map<Key, Storage<*, *>>,
) : JsonSerializer<Storage<*, *>>, JsonDeserializer<Storage<*, *>> {

    override fun serialize(storage: Storage<*, *>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(storage.id.asString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Storage<*, *> {
        check(json is JsonPrimitive && json.isString) { "Storage must be a json string" }
        val id = Key.key(json.asString)
        return storageProvider()[id] ?: run {
            throw IllegalArgumentException("Unknown storage '$id'")
        }
    }
}
