package net.voxelpi.varp.api.warp.provider.compositor

import net.voxelpi.varp.api.warp.provider.TreeProvider

interface TreeCompositor : TreeProvider {

    fun mounts(): Collection<TreeCompositorMount>
}
