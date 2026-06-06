package net.voxelpi.varp.repository

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope

public interface StorageHandle {

    /**
     * The event scope of the storage handle.
     */
    public val eventScope: EventScope

    @JvmRecord
    public data class Simple(
        override val eventScope: EventScope = eventScope(),
    ) : StorageHandle
}
