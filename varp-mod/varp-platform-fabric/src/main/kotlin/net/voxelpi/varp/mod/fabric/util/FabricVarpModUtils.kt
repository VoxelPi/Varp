package net.voxelpi.varp.mod.fabric.util

import net.minecraft.entity.EntityPosition
import net.minecraft.server.world.ServerWorld
import net.voxelpi.varp.MinecraftLocation

fun minecraftLocation(world: ServerWorld, position: EntityPosition): MinecraftLocation {
    return MinecraftLocation(
        world.registryKey.key(),
        position.position().x,
        position.position().y,
        position.position().z,
        position.yaw(),
        position.pitch()
    )
}
