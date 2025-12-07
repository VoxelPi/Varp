package net.voxelpi.varp.repository.compositor

import net.voxelpi.varp.repository.RepositoryType

public object CompositorType : RepositoryType<Compositor, CompositorConfig>("compositor", Compositor::class, CompositorConfig::class) {

    override fun create(id: String, config: CompositorConfig): Result<Compositor> {
        return Result.success(Compositor(id, config))
    }
}
