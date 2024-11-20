package net.voxelpi.varp.mod.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.audience.Audience
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack

@Suppress("UnstableApiUsage")
class PaperVarpCommandSourceStack(
    val sourceStack: CommandSourceStack,
) : VarpCommandSourceStack {

    override val location: MinecraftLocation
        get() = sourceStack.location.varpLocation()

    override val sender: Audience
        get() = sourceStack.sender
}
