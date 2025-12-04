package net.voxelpi.varp.mod.fabric.server.command

import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.audience.Audience
import net.minecraft.server.command.ServerCommandSource
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.entity.VarpServerEntity
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import net.voxelpi.varp.mod.server.command.VarpCommandSourceStack

class FabricVarpCommandSourceStack(
    override val server: VarpServerImpl,
    val sourceStack: ServerCommandSource,
) : VarpCommandSourceStack {

    override val location: MinecraftLocation
        get() = MinecraftLocation(
            sourceStack.world.registryKey.key(),
            sourceStack.position.x,
            sourceStack.position.y,
            sourceStack.position.z,
            sourceStack.rotation.y,
            sourceStack.rotation.x,
        )

    override val sender: Audience
        get() = sourceStack

    override fun playerOrNull(): VarpServerPlayer? {
        val player = sourceStack.player ?: return null
        return FabricVarpMod.varpServer?.playerService?.player(player)
    }

    override fun entityOrNull(): VarpServerEntity? {
        val entity = sourceStack.entity ?: return null
        return FabricVarpMod.varpServer?.entityService?.entity(entity)
    }

    override fun hasPermission(permission: String?): Boolean {
        if (permission == null) {
            return true
        }

        return Permissions.check(sourceStack, permission, 2)
    }
}
