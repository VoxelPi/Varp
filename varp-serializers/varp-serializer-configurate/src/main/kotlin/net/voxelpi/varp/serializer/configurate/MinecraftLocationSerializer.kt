package net.voxelpi.varp.serializer.configurate

import net.kyori.adventure.key.Key
import net.voxelpi.varp.MinecraftLocation
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

public object MinecraftLocationSerializer : TypeSerializer<MinecraftLocation> {

    override fun serialize(type: Type, obj: MinecraftLocation?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }

        node.node("world").set(obj.world)
        node.node("x").set(obj.x)
        node.node("y").set(obj.y)
        node.node("z").set(obj.z)
        node.node("yaw").set(obj.yaw)
        node.node("pitch").set(obj.pitch)
    }

    override fun deserialize(type: Type, node: ConfigurationNode): MinecraftLocation? {
        if (node.empty()) {
            return null
        }

        val world = node.node("world").get<Key>() ?: throw SerializationException("Location is missing world id")
        val x = node.node("x").double
        val y = node.node("y").double
        val z = node.node("z").double
        val yaw = node.node("yaw").float
        val pitch = node.node("pitch").float
        return MinecraftLocation(world, x, y, z, yaw, pitch)
    }
}
