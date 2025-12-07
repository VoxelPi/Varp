package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.tree.path.NodePath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object NodePathSerializer : ScalarSerializer<NodePath>(NodePath::class.java) {

    override fun deserialize(type: Type, obj: Any?): NodePath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        return NodePath.parse(obj.toString()).getOrElse { throw SerializationException(it) }
    }

    override fun serialize(item: NodePath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
