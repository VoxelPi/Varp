package net.voxelpi.varp.mod.server.api.player

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identified
import net.kyori.adventure.text.Component
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.api.VarpClientInformation
import java.util.UUID

/**
 * A player on the server.
 */
public interface VarpServerPlayer : Audience, Identified {

    /**
     * The uniqueId of the player.
     */
    public val uniqueId: UUID

    /**
     * The username of the player.
     */
    public val username: String

    /**
     * The display name of the player.
     */
    public val displayName: Component

    /**
     * The location of the player.
     */
    public val location: MinecraftLocation

    /**
     * Information about the varp mod installed on the client,
     * or null if the varp mod is not installed on the client.
     */
    public val clientInformation: VarpClientInformation?

    /**
     * Returns if the player has the varp mod installed on their client.
     */
    public fun hasClientsideVarpSupport(): Boolean {
        return clientInformation != null
    }

    /**
     * The teleportation log of the given player.
     */
    public val teleportationLog: TeleportationLog

    /**
     * Reverts the given number of steps of the players teleportation log.
     *
     * @return The number of steps that were reverted, can be less than [numberOfSteps] if the log is too small.
     */
    public fun undoTeleportationLogEntries(numberOfSteps: Int): Int

    /**
     * Replays the given number of steps of the players teleportation log.
     *
     * @return The number of steps that were reverted, can be less than [numberOfSteps] if the log is too small.
     */
    public fun redoTeleportationLogEntries(numberOfSteps: Int): Int
}
