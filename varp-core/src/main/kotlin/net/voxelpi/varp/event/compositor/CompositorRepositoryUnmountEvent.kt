package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.compositor.Compositor
import net.voxelpi.varp.tree.path.NodeParentPath

/**
 * An event that is called when a repository is unmounted from a compositor.
 */
public data class CompositorRepositoryUnmountEvent(
    override val compositor: Compositor,
    val repository: Repository,
    val location: NodeParentPath,
) : CompositorEvent
