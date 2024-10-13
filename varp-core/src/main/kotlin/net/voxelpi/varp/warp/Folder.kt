package net.voxelpi.varp.warp

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.state.FolderState

public class Folder internal constructor(
    override val tree: Tree,
    path: FolderPath,
) : NodeChild, NodeParent {

    /**
     * The path to the folder.
     */
    override var path: FolderPath = path
        private set

    /**
     * The state of the folder.
     */
    override var state: FolderState
        get() = tree.folderState(path)!!
        set(value) = tree.folderState(path, value).getOrThrow()

    /**
     * Modifies the state of the folder.
     */
    public fun modify(init: FolderState.Builder.() -> Unit): FolderState {
        val builder = FolderState.Builder(state)
        builder.init()
        state = builder.build()
        return state
    }

    /**
     * Moves the folder to the given [destination].
     * Also renames the folder to the id given in the path.
     */
    public fun move(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        tree.move(path, destination, duplicatesStrategy).onFailure {
            return Result.failure(it)
        }

        path = destination
        return Result.success(Unit)
    }

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        return move(destination.folder(id), duplicatesStrategy)
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        return move(path.parent.folder(id), duplicatesStrategy)
    }

    /**
     * Copies the folder to the given [destination].
     * Also renames the folder to name given in the path.
     */
    public fun copy(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy, recursive: Boolean = true): Result<Folder> {
        return copy(destination, duplicatesStrategy, recursive, destination)
    }

    /**
     * Copies the folder to the given [destination].
     * If [recursive] is true child nodes will also be copied, otherwise only the folder itself is copied.
     */
    public fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, recursive: Boolean = true): Result<Folder> {
        return copy(destination.folder(id), duplicatesStrategy, recursive)
    }

    /**
     * Copies the folder and all its child nodes to the given [destination].
     */
    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Folder> {
        return copy(destination, duplicatesStrategy, true)
    }

    private fun copy(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy, recursive: Boolean, skipPath: FolderPath): Result<Folder> {
        // Check if the folder already exists
        tree.resolve(destination)?.let { folder ->
            when (duplicatesStrategy) {
                DuplicatesStrategy.FAIL -> return Result.failure(FolderAlreadyExistsException(folder.path))
                DuplicatesStrategy.SKIP -> return Result.success(folder)
                DuplicatesStrategy.REPLACE_EXISTING -> folder.delete()
            }
        }

        // Create the copy
        val folder = tree.createFolder(destination, state)
        if (!recursive) {
            return folder
        }

        // Copy children
        val parent = folder.getOrElse { return folder }
        for (child in childWarps()) {
            child.copy(parent.path.warp(child.id), duplicatesStrategy).onFailure {
                return Result.failure(it)
            }
        }
        for (child in childFolders()) {
            // Fix infinite recursion if creating a copy of the folder in itself or one of its children.
            if (skipPath.isSubPathOf(child.path)) {
                continue
            }

            child.copy(parent.path.folder(child.id), duplicatesStrategy, true, skipPath).onFailure {
                return Result.failure(it)
            }
        }

        return Result.success(parent)
    }

    override fun delete(): Result<Unit> {
        return tree.deleteFolder(path)
    }
}
