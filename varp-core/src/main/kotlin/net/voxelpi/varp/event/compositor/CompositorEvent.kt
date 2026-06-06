package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.compositor.Compositor
import net.voxelpi.varp.event.VarpEvent

/**
 * Base event for all compositor related events.
 */
public interface CompositorEvent : VarpEvent {

    /**
     * The affected compositor.
     */
    public val compositor: Compositor
}
