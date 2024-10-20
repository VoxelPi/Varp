package net.voxelpi.varp.mod.server.api.player

import net.kyori.adventure.identity.Identified
import net.kyori.adventure.identity.Identity
import java.util.UUID

/**
 * Manages all players that are currently online on the server.
 */
public interface VarpServerPlayerService {

    /**
     * Returns a collection of all players that are currently online on the server.
     */
    public fun players(): Collection<VarpServerPlayer>

    /**
     * Returns the player with the given [uniqueId].
     */
    public fun player(uniqueId: UUID): VarpServerPlayer?

    /**
     * Returns the player with the given [username].
     */
    public fun player(username: String): VarpServerPlayer?

    /**
     * Returns the player with the given [identity].
     */
    public fun player(identity: Identity): VarpServerPlayer?

    /**
     * Wraps the given [identified] as player.
     */
    public fun player(identified: Identified): VarpServerPlayer?
}
