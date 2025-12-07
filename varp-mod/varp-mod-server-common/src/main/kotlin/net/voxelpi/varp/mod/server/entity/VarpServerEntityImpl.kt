package net.voxelpi.varp.mod.server.entity

import net.voxelpi.varp.mod.server.api.entity.VarpServerEntity
import net.voxelpi.varp.tree.Warp

abstract class VarpServerEntityImpl : VarpServerEntity {

    override fun teleportToWarp(warp: Warp) {
        teleport(warp.location)
        // TODO: Play sound.
    }
}
