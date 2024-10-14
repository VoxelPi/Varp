package net.voxelpi.varp.mod.paper.util

import net.voxelpi.varp.MinecraftLocation
import org.bukkit.Location

fun Location.varpLocation(): MinecraftLocation {
    return MinecraftLocation(world.key, x, y, z, yaw, pitch)
}
