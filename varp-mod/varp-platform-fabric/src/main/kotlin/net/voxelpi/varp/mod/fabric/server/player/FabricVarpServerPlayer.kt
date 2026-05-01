package net.voxelpi.varp.mod.fabric.server.player

import me.lucko.fabric.api.permissions.v0.Permissions
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.WorldNotFoundException
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import java.util.UUID

class FabricVarpServerPlayer(
    override val server: FabricVarpServer,
    val player: ServerPlayer,
) : VarpServerPlayerImpl(server), ForwardingAudience.Single {

    override val uniqueId: UUID
        get() = player.uuid

    override val username: String
        get() = player.gameProfile.name

    override val displayName: Component
        get() = server.serverAudiences.nonWrappingSerializer().deserialize(player.displayName)

    override val location: MinecraftLocation
        get() = MinecraftLocation(player.level().dimension().key(), player.x, player.y, player.z, player.yRot, player.xRot)

    override fun audience(): Audience {
        return player
    }

    override fun identity(): Identity {
        return player.identity()
    }

    override fun hasPermission(permission: String?): Boolean {
        return permission == null || Permissions.check(player, permission, PermissionLevel.GAMEMASTERS)
    }

    override fun teleport(location: MinecraftLocation): Result<Unit> {
        val world = server.world(location.world) ?: return Result.failure(WorldNotFoundException(location.world))
        player.teleportTo(world, location.x, location.y, location.z, mutableSetOf(), location.yaw, location.pitch, true)
        return Result.success(Unit)
    }
}
