package net.voxelpi.varp.mod.server.player

import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayerService
import java.util.UUID

abstract class VarpServerPlayerServiceImpl<P : VarpServerPlayerImpl>(
    open val server: VarpServerImpl,
) : VarpServerPlayerService {

    protected val players: MutableMap<UUID, P> = mutableMapOf()

    override fun players(): Collection<P> {
        return players.values
    }

    override fun player(uniqueId: UUID): P? {
        return players[uniqueId]
    }

    override fun player(username: String): P? {
        return players.values.find { it.username == username }
    }

    override fun player(identity: Identity): P? {
        return players[identity.uuid()]
    }

    override fun player(identified: Identified): P? {
        return players[identified.identity().uuid()]
    }
}
