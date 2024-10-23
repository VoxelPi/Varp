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
    override val state: FolderState
        get() = tree.folderState(path)!!

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(state: FolderState): Result<FolderState> {
        tree.folderState(path, state).getOrElse {
            return Result.failure(it)
        }
        return Result.success(state)
    }

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(init: FolderState.Builder.() -> Unit): Result<FolderState> {
        val builder = FolderState.Builder(state)
        builder.init()
        return modify(builder.build())
    }

    /**
     * Moves the folder to the given [destination].
     * Also renames the folder to the id given in the path.
     */
    public suspend fun move(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        tree.move(path, destination, duplicatesStrategy).onFailure {
            return Result.failure(it)
        }

        path = destination
        return Result.success(Unit)
    }

    override suspend fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String?): Result<Unit> {
        return move(destination.folder(destinationId ?: id), duplicatesStrategy)
    }

    override suspend fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        return move(path.parent.folder(id), duplicatesStrategy)
    }

    /**
     * Copies the folder to the given [destination].
     * Also renames the folder to name given in the path.
     */
    public suspend fun copy(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy, recursive: Boolean = true): Result<Folder> {
        return copy(destination, duplicatesStrategy, recursive, destination)
    }

    /**
     * Copies the folder to the given [destination].
     * If [recursive] is true child nodes will also be copied, otherwise only the folder itself is copied.
     */
    public suspend fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String? = null, recursive: Boolean = true): Result<Folder> {
        return copy(destination.folder(destinationId ?: id), duplicatesStrategy, recursive)
    }

    /**
     * Copies the folder and all its child nodes to the given [destination].
     */
    override suspend fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String?): Result<Folder> {
        return copy(destination, duplicatesStrategy, destinationId, true)
    }

    private suspend fun copy(destination: FolderPath, duplicatesStrategy: DuplicatesStrategy, recursive: Boolean, skipPath: FolderPath): Result<Folder> {
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

    override suspend fun delete(): Result<Unit> {
        return tree.deleteFolder(path)
    }
}
