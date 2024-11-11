package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.repository.RepositoryType

public object CompositorType : RepositoryType<Compositor, CompositorConfig>("compositor", Compositor::class, CompositorConfig::class) {

    override fun create(id: String, config: CompositorConfig): Result<Compositor> {
        return Result.success(Compositor(id, config))
    }
}
