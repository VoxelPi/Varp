package net.voxelpi.varp.serializer.configurate

import net.voxelpi.varp.warp.path.FolderPath
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

public object FolderPathSerializer : ScalarSerializer<FolderPath>(FolderPath::class.java) {

    override fun deserialize(type: Type, obj: Any?): FolderPath? {
        if (obj !is CharSequence) {
            throw SerializationException("Expected a String, but got ${obj?.javaClass?.name}")
        }
        return FolderPath.parse(obj.toString()).getOrElse { throw SerializationException(it) }
    }

    override fun serialize(item: FolderPath?, typeSupported: Predicate<Class<*>?>): Any? {
        return item?.toString()
    }
}
