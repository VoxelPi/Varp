package net.voxelpi.varp.warp.provider.compositor

import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.provider.compositor.TreeCompositor
import net.voxelpi.varp.api.warp.provider.compositor.TreeCompositorMount
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.RootState
import net.voxelpi.varp.api.warp.state.TreeStateRegistry
import net.voxelpi.varp.api.warp.state.WarpState

class VarpTreeCompositor : TreeCompositor {

    override fun mounts(): Collection<TreeCompositorMount> {
        TODO("Not yet implemented")
    }

    override val registry: TreeStateRegistry
        get() = TODO("Not yet implemented")

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun saveRootState(state: RootState): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        TODO("Not yet implemented")
    }
}
