package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.warp.path.NodePath
import java.lang.reflect.Type

public object NodePathSerializer : JsonSerializer<NodePath>, JsonDeserializer<NodePath> {

    override fun serialize(src: NodePath?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (src == null) {
            return null
        }
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): NodePath? {
        if (json == null || json.isJsonNull) {
            return null
        }
        return NodePath.parse(json.asJsonPrimitive.asString).getOrThrow()
    }
}
