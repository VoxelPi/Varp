package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A varp repository. This is the datasource for a varp tree.
 *
 * @property id The id of the repository.
 */
public abstract class Repository(
    public val id: String,
) {

    /**
     * The type of the repository.
     */
    public abstract val type: RepositoryType<*, *>

    /**
     * The config of the repository.
     */
    public abstract val config: RepositoryConfig

    public abstract val registryView: TreeStateRegistryView

    /**
     * The tree of this repository.
     */
    public val tree: Tree = Tree(this)

    /**
     * Function that is called when the repository is activated.
     */
    public open suspend fun activate(): Result<Unit> {
        logger.debug("Activating repository {} (type: {})", id, type.id)
        return Result.success(Unit)
    }

    /**
     * Function that is called when the repository is activated.
     */
    public open suspend fun deactivate(): Result<Unit> {
        logger.debug("Deactivating repository {} (type: {})", id, type.id)
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

    public companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
