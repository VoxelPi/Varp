package net.voxelpi.varp.mod.paper.command

import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.audience.Audience
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServer
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack
import net.voxelpi.varp.mod.server.entity.VarpServerEntityImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import org.bukkit.entity.Player

class PaperVarpCommandSourceStack(
    override val server: VarpServerImpl,
    val sourceStack: CommandSourceStack,
) : VarpCommandSourceStack {

    override val location: MinecraftLocation
        get() = sourceStack.location.varpLocation()

    override val sender: Audience
        get() = sourceStack.sender

    override fun playerOrNull(): VarpServerPlayerImpl? {
        val player = sourceStack.executor as? Player ?: return null
        return (VarpServer.get() as PaperVarpServer).playerService.player(player)
    }

    override fun entityOrNull(): VarpServerEntityImpl? {
        val entity = sourceStack.executor ?: return null
        return (VarpServer.get() as PaperVarpServer).entityService.entity(entity)
    }

    override fun hasPermission(permission: String?): Boolean {
        if (permission == null) {
            return true
        }
        return sourceStack.sender.hasPermission(permission)
    }
}
