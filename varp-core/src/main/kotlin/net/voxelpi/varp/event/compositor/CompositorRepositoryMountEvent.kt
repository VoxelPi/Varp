package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.compositor.Compositor
import net.voxelpi.varp.compositor.CompositorMount

/**
 * An event that is called when a repository is mounted in a compositor.
 */
public data class CompositorRepositoryMountEvent(
    override val compositor: Compositor,
    val mount: CompositorMount,
) : CompositorEvent
