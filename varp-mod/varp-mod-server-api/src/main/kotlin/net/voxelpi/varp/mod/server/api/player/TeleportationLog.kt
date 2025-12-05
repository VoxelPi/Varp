package net.voxelpi.varp.mod.server.api.player

import net.voxelpi.varp.MinecraftLocation

/**
 * The teleportation log of a player.
 * Every time a player is teleported, an entry is added to this log with the start & end location of the teleportation.
 */
public interface TeleportationLog {

    /**
     * All entries of this teleportation log.
     */
    public val entries: Collection<Entry>

    /**
     * An entry of a teleportation log.
     * @property from The start location of the teleportation.
     * @property to The end location of the teleportation.
     */
    public data class Entry(
        public val from: MinecraftLocation,
        public val to: MinecraftLocation,
    )
}
