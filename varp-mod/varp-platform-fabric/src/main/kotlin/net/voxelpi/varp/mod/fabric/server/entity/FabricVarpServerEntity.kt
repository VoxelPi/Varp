package net.voxelpi.varp.mod.fabric.server.entity

import net.kyori.adventure.text.Component
import net.minecraft.world.entity.Entity
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.WorldNotFoundException
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import java.util.UUID

class FabricVarpServerEntity(
    val server: FabricVarpServer,
    val entity: Entity,
) : VarpServerEntityImpl() {

    override val uniqueId: UUID
        get() = entity.uuid

    override val name: Component
        get() = server.serverAudiences.asAdventure(entity.name)

    override val location: MinecraftLocation
        get() = MinecraftLocation(entity.level().dimension().key(), entity.x, entity.y, entity.z, entity.yRot, entity.xRot)

    override fun teleport(location: MinecraftLocation) {
        val world = server.world(location.world) ?: throw WorldNotFoundException(location.world)
        entity.teleportTo(world, location.x, location.y, location.z, emptySet(), location.yaw, location.pitch, true)
    }
}
