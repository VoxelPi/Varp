package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.findAnnotation

public abstract class Repository(
    public val id: String,
) {

    public abstract val registryView: TreeStateRegistryView

    /**
     * The tree of this repository.
     */
    public val tree: Tree = Tree(this)

    /**
     * Function that is called when the repository is activated.
     */
    public open suspend fun activate(): Result<Unit> {
        logger.debug("Activating repository {} (type: {})", id, typeId())
        return Result.success(Unit)
    }

    /**
     * Function that is called when the repository is activated.
     */
    public open suspend fun deactivate(): Result<Unit> {
        logger.debug("Deactivating repository {} (type: {})", id, typeId())
        return Result.success(Unit)
    }

    /**
     * Reloads the content of the repository.
     * The implementation should also post a [net.voxelpi.varp.event.repository.RepositoryLoadEvent] to the tree event bus.
     */
    public abstract suspend fun load(): Result<Unit>

    public abstract suspend fun create(path: WarpPath, state: WarpState): Result<Unit>

    public abstract suspend fun create(path: FolderPath, state: FolderState): Result<Unit>

    public abstract suspend fun save(path: WarpPath, state: WarpState): Result<Unit>

    public abstract suspend fun save(path: FolderPath, state: FolderState): Result<Unit>

    public abstract suspend fun save(state: FolderState): Result<Unit>

    public abstract suspend fun delete(path: WarpPath): Result<Unit>

    public abstract suspend fun delete(path: FolderPath): Result<Unit>

    public abstract suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit>

    public abstract suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit>

    /**
     * Gets the type id of this tree repository specified via the [RepositoryType] annotation.
     * If this repository is not annotated with that annotation, null is returned.
     */
    public fun typeId(): String? {
        return this::class.findAnnotation<RepositoryType>()?.id
    }

    public companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
