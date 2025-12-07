package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.tree.path.RootPath
import java.lang.reflect.Type

public object RootPathSerializer : JsonSerializer<RootPath>, JsonDeserializer<RootPath> {

    override fun serialize(src: RootPath?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (src == null) {
            return null
        }
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): RootPath? {
        if (json == null || json.isJsonNull) {
            return null
        }
        require(json.asJsonPrimitive.asString == "/")
        return RootPath
    }
}
