package net.voxelpi.varp.serializer.configurate

import net.kyori.adventure.text.Component
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.warp.state.WarpState
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

public object WarpStateSerializer : TypeSerializer<WarpState> {

    override fun serialize(type: Type, obj: WarpState?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }

        node.node("name").set(obj.name)
        node.node("description").setList(Component::class.java, obj.description)
        node.node("tags").setList(String::class.java, obj.tags.toList())
        obj.properties.forEach { (key, value) ->
            node.node("properties", key).set(value)
        }
        node.node("location").set(obj.location)
    }

    override fun deserialize(type: Type, node: ConfigurationNode): WarpState? {
        if (node.empty()) {
            return null
        }

        val name = node.node("name").get<Component>() ?: throw SerializationException("Folder has no name")
        val description = node.node("description").getList(Component::class.java, mutableListOf())
        val tags = node.node("tags").getList(String::class.java, mutableListOf()).toSet()
        val properties = node.node("properties").childrenMap().map { (key, value) ->
            (key as String) to value.getString("")
        }.toMap()
        val location = node.node("location").get<MinecraftLocation>()
            ?: throw SerializationException("Missing location property")
        return WarpState(location, name, description, tags, properties)
    }
}
