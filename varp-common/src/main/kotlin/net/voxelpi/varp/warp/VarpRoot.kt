package net.voxelpi.varp.warp

import net.voxelpi.varp.api.warp.Root
import net.voxelpi.varp.api.warp.path.RootPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.warp.tree.VarpNodeParent

class VarpRoot(
    override val tree: VarpTree,
) : Root, VarpNodeParent {

    override val path: RootPath
        get() = RootPath

    override val state: FolderState
        get() = tree.rootState()
}
