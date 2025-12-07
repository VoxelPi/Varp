package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.repository.Repository
import net.voxelpi.varp.repository.compositor.Compositor
import net.voxelpi.varp.tree.path.NodeParentPath

/**
 * An event that is called when a repository is mounted in a compositor.
 */
public data class CompositorRepositoryMountEvent(
    override val compositor: Compositor,
    val repository: Repository,
    val location: NodeParentPath,
) : CompositorEvent
