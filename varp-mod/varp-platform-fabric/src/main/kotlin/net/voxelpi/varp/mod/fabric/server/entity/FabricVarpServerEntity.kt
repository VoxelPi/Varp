package net.voxelpi.varp.mod.fabric.server.entity

import net.minecraft.entity.Entity
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.WorldNotFoundException
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import net.voxelpi.varp.warp.Warp
import java.util.UUID

class FabricVarpServerEntity(
    val server: FabricVarpServer,
    val entity: Entity,
) : VarpServerEntityImpl() {

    override val uniqueId: UUID
        get() = entity.uuid

    override val location: MinecraftLocation
        get() = MinecraftLocation(entity.world.registryKey.key(), entity.x, entity.y, entity.z, entity.yaw, entity.pitch)

    override fun teleport(location: MinecraftLocation) {
        val world = server.world(location.world) ?: throw WorldNotFoundException(location.world)
        entity.teleport(world, location.x, location.y, location.z, emptySet(), location.yaw, location.pitch, true)
    }

    override fun teleportToWarp(warp: Warp) {
        teleport(warp.location)
        // TODO: Play sound.
    }
}
