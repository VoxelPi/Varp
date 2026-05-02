package net.voxelpi.varp.serializer.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.voxelpi.varp.ComponentTemplate
import java.lang.reflect.Type

public object ComponentTemplateSerializer : JsonSerializer<ComponentTemplate>, JsonDeserializer<ComponentTemplate> {

    override fun serialize(src: ComponentTemplate?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement? {
        if (src == null) {
            return null
        }
        return JsonPrimitive(src.originalMessage)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): ComponentTemplate? {
        if (json == null || json.isJsonNull) {
            return null
        }
        return ComponentTemplate(json.asJsonPrimitive.asString)
    }
}
