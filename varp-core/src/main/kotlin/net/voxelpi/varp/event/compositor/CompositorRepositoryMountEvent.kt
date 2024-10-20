package net.voxelpi.varp.event.compositor

import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.repository.Repository
import net.voxelpi.varp.warp.repository.compositor.Compositor

/**
 * An event that is called when a repository is mounted in a compositor.
 */
public data class CompositorRepositoryMountEvent(
    override val compositor: Compositor,
    val repository: Repository,
    val location: NodeParentPath,
) : CompositorEvent
