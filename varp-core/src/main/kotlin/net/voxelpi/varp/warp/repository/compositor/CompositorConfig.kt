package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.repository.RepositoryConfig

public data class CompositorConfig(
    public val mounts: List<CompositorMount>,
) : RepositoryConfig
