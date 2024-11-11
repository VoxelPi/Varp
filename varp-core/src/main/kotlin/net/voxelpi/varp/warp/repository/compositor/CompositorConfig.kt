package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.repository.RepositoryConfig

public data class CompositorConfig(
    public val mounts: List<CompositorMount>,
) : RepositoryConfig {

    public companion object {

        public val EMPTY: CompositorConfig = CompositorConfig(emptyList())
    }
}
