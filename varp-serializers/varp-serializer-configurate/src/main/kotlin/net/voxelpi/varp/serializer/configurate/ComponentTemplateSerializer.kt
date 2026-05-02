package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.ComponentTemplate
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object ComponentTemplateSerializer : ScalarSerializer<ComponentTemplate>(ComponentTemplate::class.java) {

    override fun deserialize(type: Type, obj: Any?): ComponentTemplate? {
        if (obj == null) {
            return null
        }
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj.javaClass.name}")
        }
        return ComponentTemplate(obj.toString())
    }

    override fun serialize(item: ComponentTemplate?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.originalMessage
    }
}
