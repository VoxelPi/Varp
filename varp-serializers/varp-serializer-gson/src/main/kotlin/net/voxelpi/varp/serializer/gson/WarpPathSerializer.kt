package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.warp.path.WarpPath
import java.lang.reflect.Type

public object WarpPathSerializer : JsonSerializer<WarpPath>, JsonDeserializer<WarpPath> {

    override fun serialize(src: WarpPath?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (src == null) {
            return null
        }
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): WarpPath? {
        if (json == null || json.isJsonNull) {
            return null
        }
        return WarpPath.parse(json.asJsonPrimitive.asString).getOrThrow()
    }
}
