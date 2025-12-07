package net.voxelpi.varp.repository.compositor

import net.voxelpi.varp.repository.RepositoryConfig

public data class CompositorConfig(
    public val mounts: List<CompositorMount>,
) : RepositoryConfig {

    public companion object {

        public val EMPTY: CompositorConfig = CompositorConfig(emptyList())
    }
}
