package net.voxelpi.varp.mod.server.api.entity

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.warp.Warp
import java.util.UUID

public interface VarpServerEntity {

    public val uniqueId: UUID

    public val location: MinecraftLocation

    public fun teleport(location: MinecraftLocation)

    public fun teleportToWarp(warp: Warp)
}
