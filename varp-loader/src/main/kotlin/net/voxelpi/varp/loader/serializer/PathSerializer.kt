package net.voxelpi.varp.loader.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.nio.file.Path

internal class PathSerializer(
    val rootPath: Path,
) : JsonSerializer<Path>, JsonDeserializer<Path> {

    override fun serialize(src: Path, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        if (src.isAbsolute) {
            require(src.startsWith(rootPath)) { "Path not inside \"$rootPath\"" }
            return JsonPrimitive(rootPath.relativize(src).toString())
        } else {
            return JsonPrimitive(src.toString())
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Path {
        return rootPath.resolve(json.asString)
    }
}
