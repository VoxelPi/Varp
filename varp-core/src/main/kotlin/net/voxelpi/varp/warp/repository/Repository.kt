package net.voxelpi.varp.warp.repository

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.NodeParentAlreadyExistsException
import net.voxelpi.varp.option.DuplicatesStrategyOption
import net.voxelpi.varp.option.OptionsContext
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath
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

    public suspend fun save(path: NodeParentPath, state: FolderState): Result<Unit> {
        return when (path) {
            is FolderPath -> save(path, state)
            RootPath -> save(state)
        }
    }

    public abstract suspend fun delete(path: WarpPath): Result<Unit>

    public abstract suspend fun delete(path: FolderPath): Result<Unit>

    public abstract suspend fun move(src: WarpPath, dst: WarpPath, options: OptionsContext): Result<Unit>

    public abstract suspend fun move(src: FolderPath, dst: FolderPath, options: OptionsContext): Result<Unit>

    public open suspend fun move(src: FolderPath, dst: NodeParentPath, options: OptionsContext): Result<Unit> {
        return when (dst) {
            is FolderPath -> {
                move(src, dst, options)
            }
            RootPath -> {
                val duplicatesStrategy = options.getOrDefault(DuplicatesStrategyOption)
                if (duplicatesStrategy == DuplicatesStrategy.FAIL) {
                    return Result.failure(NodeParentAlreadyExistsException(dst))
                }

                // If replace strategy is used, overwrite root node state.
                if (duplicatesStrategy == DuplicatesStrategy.REPLACE_EXISTING) {
                    val srcState = registryView[src] ?: throw FolderNotFoundException(src)
                    save(srcState)
                }

                // Move all direct child folders
                val directChildFolders = registryView.folders.keys
                    .filter { src.isTrueSubPathOf(it) && !it.value.substring(src.value.length, it.value.length - 1).contains("/") }
                for (folder in directChildFolders) {
                    move(folder, RootPath / (folder.relativeTo(src)!! as FolderPath), options).getOrElse { return Result.failure(it) }
                }

                // Move all direct child warps.
                val directChildWarps = registryView.warps.keys
                    .filter { src.isSubPathOf(it) && !it.value.substring(src.value.length).contains("/") }
                for (warp in directChildWarps) {
                    move(warp, RootPath / warp.relativeTo(src)!!, options).getOrElse { return Result.failure(it) }
                }

                // Everything completed successfully.
                Result.success(Unit)
            }
        }
    }

    public companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
