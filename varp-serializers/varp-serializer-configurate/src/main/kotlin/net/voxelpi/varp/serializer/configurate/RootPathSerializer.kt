package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.warp.path.RootPath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object RootPathSerializer : ScalarSerializer<RootPath>(RootPath::class.java) {

    override fun deserialize(type: Type, obj: Any?): RootPath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        if (obj.toString() != "/") {
            throw SerializationException("Expected a root path, but got $obj")
        }
        return RootPath
    }

    override fun serialize(item: RootPath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
