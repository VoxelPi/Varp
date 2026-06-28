package net.voxelpi.varp.repository

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.tree.state.TreeState

public interface StorageHandle {

    /**
     * The default state of the tree that is stored in the storage.
     */
    public val defaultState: TreeState

    /**
     * The event scope of the storage handle.
     */
    public val eventScope: EventScope

    @JvmRecord
    public data class Simple(
        override val defaultState: TreeState,
        override val eventScope: EventScope = eventScope(),
    ) : StorageHandle
}
