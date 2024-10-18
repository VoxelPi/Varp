package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.repository.RepositoryConfig

public data class TreeCompositorConfig(
    public val mounts: List<TreeCompositorMount>,
) : RepositoryConfig
