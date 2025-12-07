package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.event.VarpEvent
import net.voxelpi.varp.repository.compositor.Compositor

/**
 * Base event for all compositor related events.
 */
public interface CompositorEvent : VarpEvent {

    /**
     * The affected compositor.
     */
    public val compositor: Compositor
}
