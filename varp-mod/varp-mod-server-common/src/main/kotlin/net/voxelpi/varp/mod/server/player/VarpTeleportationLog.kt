package net.voxelpi.varp.mod.server.player

import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.server.api.player.TeleportationLog

class VarpTeleportationLog(
    entries: Collection<TeleportationLog.Entry> = emptyList(),
    private var index: Int = 0,
) : TeleportationLog {

    init {
        require(0 <= index) { "Invalid TeleportationLog entry index, < 0" }
        require(index <= entries.size) { "Invalid TeleportationLog entry index, > n" }
    }

    override val entries: ArrayDeque<TeleportationLog.Entry> = ArrayDeque(entries)

    fun logTeleport(from: MinecraftLocation, to: MinecraftLocation) {
        // Clear the following history, if the player is currently not at the head of their teleportation log.
        repeat(index) {
            entries.removeLast()
        }
        index = 0

        // Store the teleport log entry
        entries.addLast(TeleportationLog.Entry(from, to))
    }

    fun undoEntry(): MinecraftLocation? {
        if (index >= entries.size) {
            return null
        }
        val location = entries[entries.size - 1 - index].from
        ++index
        return location
    }

    fun redoEntry(): MinecraftLocation? {
        if (index <= 0) {
            return null
        }
        --index
        val location = entries[entries.size - 1 - index].to
        return location
    }
}
