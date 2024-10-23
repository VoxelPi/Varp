package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState
import kotlin.reflect.full.findAnnotation

public interface Repository {

    public val id: String

    public val registryView: TreeStateRegistryView

    /**
     * Function that is called when the repository is activated.
     */
    public suspend fun activate(): Result<Unit> {
        return Result.success(Unit)
    }

    /**
     * Function that is called when the repository is activated.
     */
    public suspend fun deactivate(): Result<Unit> {
        return Result.success(Unit)
    }

    /**
     * Reloads the content of the repository
     */
    public suspend fun load(): Result<Unit>

    public suspend fun create(path: WarpPath, state: WarpState): Result<Unit>

    public suspend fun create(path: FolderPath, state: FolderState): Result<Unit>

    public suspend fun save(path: WarpPath, state: WarpState): Result<Unit>

    public suspend fun save(path: FolderPath, state: FolderState): Result<Unit>

    public suspend fun save(state: FolderState): Result<Unit>

    public suspend fun delete(path: WarpPath): Result<Unit>

    public suspend fun delete(path: FolderPath): Result<Unit>

    public suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit>

    public suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit>

    /**
     * Gets the type id of this tree repository specified via the [RepositoryType] annotation.
     * If this repository is not annotated with that annotation, null is returned.
     */
    public fun typeId(): String? {
        return this::class.findAnnotation<RepositoryType>()?.id
    }
}
