package net.voxelpi.varp.mod.paper.util

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.WorldNotFoundException
import org.bukkit.Bukkit
import org.bukkit.Location

fun Location.varpLocation(): MinecraftLocation {
    return MinecraftLocation(world.key, x, y, z, yaw, pitch)
}

fun MinecraftLocation.paperLocation(): Result<Location> {
    val world = Bukkit.getWorld(world) ?: return Result.failure(WorldNotFoundException(world))
    return Result.success(Location(world, x, y, z, yaw, pitch))
}
