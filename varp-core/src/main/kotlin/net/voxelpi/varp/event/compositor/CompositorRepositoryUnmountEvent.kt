package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.compositor.Compositor
import net.voxelpi.varp.compositor.CompositorMount

/**
 * An event that is called when a repository is unmounted from a compositor.
 */
public data class CompositorRepositoryUnmountEvent(
    override val compositor: Compositor,
    val mount: CompositorMount,
) : CompositorEvent
