package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.warp.path.NodeParentPath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object NodeParentPathSerializer : ScalarSerializer<NodeParentPath>(NodeParentPath::class.java) {

    override fun deserialize(type: Type, obj: Any?): NodeParentPath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        return NodeParentPath.parse(obj.toString()).getOrElse { throw SerializationException(it) }
    }

    override fun serialize(item: NodeParentPath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
