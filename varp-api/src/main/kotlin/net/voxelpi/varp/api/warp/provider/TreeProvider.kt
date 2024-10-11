package net.voxelpi.varp.api.warp.provider

import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.TreeStateRegistryView
import net.voxelpi.varp.api.warp.state.WarpState

interface TreeProvider {

    val registry: TreeStateRegistryView

    fun createWarpState(path: WarpPath, state: WarpState): Result<Unit>

    fun createFolderState(path: FolderPath, state: FolderState): Result<Unit>

    fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit>

    fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit>

    fun saveRootState(state: FolderState): Result<Unit>

    fun deleteWarpState(path: WarpPath): Result<Unit>

    fun deleteFolderState(path: FolderPath): Result<Unit>

    fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit>

    fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit>
}
