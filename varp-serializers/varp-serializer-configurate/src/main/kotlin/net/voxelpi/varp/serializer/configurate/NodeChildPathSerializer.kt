package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.warp.path.NodeChildPath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object NodeChildPathSerializer : ScalarSerializer<NodeChildPath>(NodeChildPath::class.java) {

    override fun deserialize(type: Type, obj: Any?): NodeChildPath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        return NodeChildPath.parse(obj.toString()).getOrElse { throw SerializationException(it) }
    }

    override fun serialize(item: NodeChildPath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
