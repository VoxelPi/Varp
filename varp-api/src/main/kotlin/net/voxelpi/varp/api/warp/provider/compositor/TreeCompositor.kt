package net.voxelpi.varp.api.warp.provider.compositor

import net.voxelpi.varp.api.warp.path.NodePath
import net.voxelpi.varp.api.warp.provider.TreeProvider

interface TreeCompositor : TreeProvider {

    fun mounts(): Collection<TreeCompositorMount>

    fun mountAt(path: NodePath): TreeCompositorMount
}
