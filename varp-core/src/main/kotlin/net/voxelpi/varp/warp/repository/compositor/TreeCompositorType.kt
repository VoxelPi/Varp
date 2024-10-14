package net.voxelpi.varp.warp.repository.compositor

import net.voxelpi.varp.warp.repository.TreeRepositoryType

public object TreeCompositorType : TreeRepositoryType<TreeCompositor, TreeCompositorConfig> {

    override val id: String = "compositor"

    override fun createRepository(id: String, config: TreeCompositorConfig): TreeCompositor {
        return TreeCompositor(id, config.mounts)
    }
}
