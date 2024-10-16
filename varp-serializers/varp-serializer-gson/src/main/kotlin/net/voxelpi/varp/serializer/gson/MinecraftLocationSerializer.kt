package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.key.Key
import net.voxelpi.varp.MinecraftLocation
import java.lang.reflect.Type

public object MinecraftLocationSerializer : JsonSerializer<MinecraftLocation>, JsonDeserializer<MinecraftLocation> {

    override fun serialize(src: MinecraftLocation?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (src == null) {
            return JsonNull.INSTANCE
        }

        return JsonObject().apply {
            addProperty("world", src.world.asString())
            addProperty("x", src.x)
            addProperty("y", src.y)
            addProperty("z", src.z)
            addProperty("yaw", src.yaw)
            addProperty("pitch", src.pitch)
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): MinecraftLocation? {
        if (json == null || json.isJsonNull) {
            return null
        }
        require(json is JsonObject) { "Expected location to be an json object." }

        val world = Key.key(json.getAsJsonPrimitive("world").asString)
        val x = json.getAsJsonPrimitive("x").asDouble
        val y = json.getAsJsonPrimitive("y").asDouble
        val z = json.getAsJsonPrimitive("z").asDouble
        val yaw = json.getAsJsonPrimitive("yaw").asFloat
        val pitch = json.getAsJsonPrimitive("pitch").asFloat
        return MinecraftLocation(world, x, y, z, yaw, pitch)
    }
}
