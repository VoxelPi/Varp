package net.voxelpi.varp.mod.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.audience.Audience
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.mod.server.api.entity.VarpServerEntity
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
class PaperVarpCommandSourceStack(
    val sourceStack: CommandSourceStack,
) : VarpCommandSourceStack {

    override val location: MinecraftLocation
        get() = sourceStack.location.varpLocation()

    override val sender: Audience
        get() = sourceStack.sender

    override fun playerOrNull(): VarpServerPlayer? {
        val player = sourceStack.executor as? Player ?: return null
        return (VarpServerAPI.get() as PaperVarpServer).playerService.player(player)
    }

    override fun entityOrNull(): VarpServerEntity? {
        val entity = sourceStack.executor ?: return null
        return (VarpServerAPI.get() as PaperVarpServer).entityService.entity(entity)
    }
}
