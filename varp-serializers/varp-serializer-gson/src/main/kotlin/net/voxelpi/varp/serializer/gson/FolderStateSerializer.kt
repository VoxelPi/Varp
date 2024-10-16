package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.text.Component
import net.voxelpi.varp.warp.state.FolderState
import java.lang.reflect.Type

public object FolderStateSerializer : JsonSerializer<FolderState>, JsonDeserializer<FolderState> {

    override fun serialize(src: FolderState?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        val json = JsonObject()
        json.add("name", context.serialize(src.name))
        json.add("description", context.serialize(src.description))
        json.add("tags", context.serialize(src.tags))
        json.add("properties", context.serialize(src.properties))
        return json
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): FolderState? {
        if (json == null || json.isJsonNull) {
            return null
        }
        require(json is JsonObject) { "Expected warp state to be an json object." }

        val name = context.deserialize<Component>(json.get("name"), Component::class.java)
        val description = context.deserialize<List<Component>>(json.get("description"), typeOf<List<Component>>())
        val tags = context.deserialize<Set<String>>(json.get("tags"), typeOf<Set<String>>())
        val properties = context.deserialize<Map<String, String>>(json.get("properties"), typeOf<Map<String, String>>())

        return FolderState(name, description, tags, properties)
    }
}
