package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState
import kotlin.reflect.full.findAnnotation

public interface Repository {

    public val id: String

    public val registry: TreeStateRegistryView

    /**
     * Function that is called when the repository is loaded.
     */
    public fun onStartup() {}

    /**
     * Function that is called when the repository is unloaded.
     */
    public fun onShutdown() {}

    /**
     * Reloads the content of the repository
     */
    public fun reload(): Result<Unit>

    public fun createWarpState(path: WarpPath, state: WarpState): Result<Unit>

    public fun createFolderState(path: FolderPath, state: FolderState): Result<Unit>

    public fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit>

    public fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit>

    public fun saveRootState(state: FolderState): Result<Unit>

    public fun deleteWarpState(path: WarpPath): Result<Unit>

    public fun deleteFolderState(path: FolderPath): Result<Unit>

    public fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit>

    public fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit>

    /**
     * Gets the type id of this tree repository specified via the [RepositoryType] annotation.
     * If this repository is not annotated with that annotation, null is returned.
     */
    public fun typeId(): String? {
        return this::class.findAnnotation<RepositoryType>()?.id
    }
}
