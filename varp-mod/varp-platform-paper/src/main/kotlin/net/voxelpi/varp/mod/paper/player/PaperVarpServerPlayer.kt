package net.voxelpi.varp.mod.paper.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import org.bukkit.entity.Player
import java.util.UUID

class PaperVarpServerPlayer(
    override val server: PaperVarpServer,
    val player: Player,
) : VarpServerPlayerImpl(server), ForwardingAudience.Single {

    override val uniqueId: UUID
        get() = player.uniqueId

    override val username: String
        get() = player.name

    override val displayName: Component
        get() = player.displayName()

    override val location: MinecraftLocation
        get() = player.location.varpLocation()

    override fun audience(): Audience {
        return player
    }

    override fun identity(): Identity {
        return player.identity()
    }
}
