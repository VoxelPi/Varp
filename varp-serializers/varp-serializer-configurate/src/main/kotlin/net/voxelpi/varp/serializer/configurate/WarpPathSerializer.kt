package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.tree.path.WarpPath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object WarpPathSerializer : ScalarSerializer<WarpPath>(WarpPath::class.java) {

    override fun deserialize(type: Type, obj: Any?): WarpPath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        return WarpPath.parse(obj.toString()).getOrElse { throw SerializationException(it) }
    }

    override fun serialize(item: WarpPath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
