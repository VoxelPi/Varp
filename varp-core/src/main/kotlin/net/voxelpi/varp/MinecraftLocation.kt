package net.voxelpi.varp

import net.kyori.adventure.key.Key

/**
 * A location in a minecraft world.
 * @property world the world of the location.
 * @property x the x coordinate of the location.
 * @property y the y coordinate of the location.
 * @property z the z coordinate of the location.
 * @property yaw the rotation about the y-axis of the location in degrees.
 * @property pitch the rotation about the x-axis of the location in degrees.
 */
@JvmRecord
public data class MinecraftLocation(
    val world: Key,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
)
