package net.voxelpi.varp.environment.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.repository.Storage
import net.voxelpi.varp.repository.StorageHandle
import net.voxelpi.varp.repository.StorageInstance
import java.lang.reflect.Type
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.javaType

internal object StorageInstanceSerializer : JsonSerializer<StorageInstance<*, *>>, JsonDeserializer<StorageInstance<*, *>> {

    override fun serialize(storage: StorageInstance<*, *>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonObject().apply {
            add("id", context.serialize(storage.storage))
            if (storageConfigObject(storage.storage) == null) {
                add("config", context.serialize(storage.config))
            }
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): StorageInstance<*, *> {
        check(json is JsonObject) { "Storage instance must be a json string" }
        val storage = context.deserialize<Storage<*, *>>(json["id"], Storage::class.java)

        val config = storageConfigObject(storage)
            ?: context.deserialize(json["config"], storage.configType.createType().javaType)

        @Suppress("UNCHECKED_CAST")
        return (storage as Storage<Any, StorageHandle>).createInstance(config)
    }

    private fun storageConfigObject(storage: Storage<*, *>): Any? {
        return storage.configType.objectInstance
    }
}
