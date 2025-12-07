package net.voxelpi.varp.event.repository

import net.voxelpi.varp.event.VarpEvent
import net.voxelpi.varp.repository.Repository

/**
 * Base event for all repository related events.
 */
public interface RepositoryEvent : VarpEvent {

    /**
     * The affected repository.
     */
    public val repository: Repository
}
