package net.voxelpi.varp.mod.server.command

import net.kyori.adventure.audience.Audience
import net.voxelpi.varp.MinecraftLocation

interface VarpCommandSourceStack {

    val location: MinecraftLocation

    val sender: Audience
}
