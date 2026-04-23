package net.voxelpi.varp.mod.fabric.util

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.PositionMoveRotation
import net.voxelpi.varp.MinecraftLocation

fun minecraftLocation(level: ServerLevel, position: PositionMoveRotation): MinecraftLocation {
    return MinecraftLocation(
        level.dimension().key(),
        position.position().x,
        position.position().y,
        position.position().z,
        position.xRot(),
        position.yRot()
    )
}
