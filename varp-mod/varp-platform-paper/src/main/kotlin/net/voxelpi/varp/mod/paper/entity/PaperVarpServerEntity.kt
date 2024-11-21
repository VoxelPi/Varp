package net.voxelpi.varp.mod.paper.entity

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.util.paperLocation
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import net.voxelpi.varp.warp.Warp
import org.bukkit.entity.Entity
import java.util.UUID

class PaperVarpServerEntity(
    val server: PaperVarpServer,
    val entity: Entity,
) : VarpServerEntityImpl() {

    override val uniqueId: UUID
        get() = entity.uniqueId

    override val location: MinecraftLocation
        get() = entity.location.varpLocation()

    override fun teleport(location: MinecraftLocation) {
        entity.teleportAsync(location.paperLocation().getOrThrow())
    }

    override fun teleportToWarp(warp: Warp) {
        teleport(warp.location)
        // TODO: Play sound.
    }
}
