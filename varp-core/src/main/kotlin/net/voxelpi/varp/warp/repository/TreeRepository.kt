package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState

public interface TreeRepository {

    public val id: String

    public val registry: TreeStateRegistryView

    public fun createWarpState(path: WarpPath, state: WarpState): Result<Unit>

    public fun createFolderState(path: FolderPath, state: FolderState): Result<Unit>

    public fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit>

    public fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit>

    public fun saveRootState(state: FolderState): Result<Unit>

    public fun deleteWarpState(path: WarpPath): Result<Unit>

    public fun deleteFolderState(path: FolderPath): Result<Unit>

    public fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit>

    public fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit>
}
