package net.voxelpi.varp.serializer.nbt

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag
import net.kyori.adventure.nbt.StringBinaryTag.stringBinaryTag
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

public object VarpNBTSerializers {

    public fun serializeMinecraftLocation(location: MinecraftLocation): CompoundBinaryTag {
        return CompoundBinaryTag.builder().apply {
            putString("dimension", location.world.asString())
            putDouble("x", location.x)
            putDouble("y", location.y)
            putDouble("z", location.z)
            putFloat("yaw", location.yaw)
            putFloat("pitch", location.pitch)
        }.build()
    }

    public fun deserializeMinecraftLocation(serialized: CompoundBinaryTag): MinecraftLocation {
        val dimension = Key.key(serialized.getString("dimension", "minecraft:overworld")!!)
        val x = serialized.getDouble("x", 0.0)
        val y = serialized.getDouble("y", 0.0)
        val z = serialized.getDouble("z", 0.0)
        val yaw = serialized.getFloat("yaw", 0.0f)
        val pitch = serialized.getFloat("pitch", 0.0f)
        return MinecraftLocation(dimension, x, y, z, yaw, pitch)
    }

    public fun serializeWarpState(warp: WarpState): CompoundBinaryTag {
        return CompoundBinaryTag.builder().apply {
            put("location", serializeMinecraftLocation(warp.location))
            putString("name", warp.name.originalMessage)
            putString("description", warp.description.joinToString("\n") { it.originalMessage })
            put("tags", ListBinaryTag.from(warp.tags.map { stringBinaryTag(it) }))
            put("properties", CompoundBinaryTag.from(warp.properties.mapValues { stringBinaryTag(it.value) }))
        }.build()
    }

    public fun deserializeWarpState(serialized: CompoundBinaryTag): WarpState {
        val name = ComponentTemplate(serialized.getString("name", "")!!)
        val description = serialized.getString("description", "")!!.split('\n').map { ComponentTemplate(it) }
        val tags = serialized.getList("tags", BinaryTagTypes.STRING).map { (it as StringBinaryTag).value() }.toSet()
        val properties = serialized.getCompound("properties").associate { (key, value) -> key to ((value as? StringBinaryTag)?.value() ?: "") }
        val location = deserializeMinecraftLocation(serialized.getCompound("location"))
        return WarpState(location, name, description, tags, properties)
    }

    public fun serializeFolderState(warp: FolderState): CompoundBinaryTag {
        return CompoundBinaryTag.builder().apply {
            putString("name", warp.name.originalMessage)
            putString("description", warp.description.joinToString("\n") { it.originalMessage })
            put("tags", ListBinaryTag.from(warp.tags.map { stringBinaryTag(it) }))
            put("properties", CompoundBinaryTag.from(warp.properties.mapValues { stringBinaryTag(it.value) }))
        }.build()
    }

    public fun deserializeFolderState(serialized: CompoundBinaryTag): FolderState {
        val name = ComponentTemplate(serialized.getString("name", "")!!)
        val description = serialized.getString("description", "")!!.split('\n').map { ComponentTemplate(it) }
        val tags = serialized.getList("tags", BinaryTagTypes.STRING).map { (it as StringBinaryTag).value() }.toSet()
        val properties = serialized.getCompound("properties").map { (key, value) -> key to (value as StringBinaryTag).value() }.toMap()

        return FolderState(name, description, tags, properties)
    }

    public fun serializeTreeState(tree: TreeState): CompoundBinaryTag {
        return CompoundBinaryTag.builder().apply {
            put("warps", CompoundBinaryTag.from(tree.warps.mapKeys { it.key.value }.mapValues { serializeWarpState(it.value) }))
            put("folders", CompoundBinaryTag.from(tree.folders.mapKeys { it.key.value }.mapValues { serializeFolderState(it.value) }))
            put("root", serializeFolderState(tree.root))
        }.build()
    }

    public fun deserializeTreeState(serialized: CompoundBinaryTag): TreeState {
        val warps = serialized.getCompound("warps").associate { WarpPath(it.key) to deserializeWarpState(it.value as CompoundBinaryTag) }
        val folders = serialized.getCompound("folders").associate { FolderPath(it.key) to deserializeFolderState(it.value as CompoundBinaryTag) }
        val root = deserializeFolderState(serialized.getCompound("root"))
        return MutableTreeState(warps.toMutableMap(), folders.toMutableMap(), root)
    }
}
