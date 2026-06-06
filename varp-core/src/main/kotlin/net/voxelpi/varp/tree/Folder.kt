package net.voxelpi.varp.tree

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.exception.tree.FolderAlreadyExistsException
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.FolderState

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
        get() = tree.state[path]!!

    /**
     * Modifies the state of the folder.
     */
    override suspend fun modify(state: FolderState): Result<FolderState> {
        return tree.update(path, state)
    }

    /**
     * Moves the folder to the given [destination].
     * Also renames the folder to the id given in the path.
     */
    public suspend fun move(
        destination: FolderPath,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Unit> = runCatching {
        tree.move(
            path,
            destination,
            duplicatesStrategy = duplicatesStrategy,
        ).getOrThrow()

        path = destination
    }

    override suspend fun moveInto(
        parent: NodeParentPath,
        id: String?,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Unit> = runCatching {
        move(
            parent.folder(id ?: this.id),
            duplicatesStrategy = duplicatesStrategy,
        ).getOrThrow()
    }

    override suspend fun move(
        id: String,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Unit> = runCatching {
        move(
            path.parent.folder(id),
            duplicatesStrategy = duplicatesStrategy,
        ).getOrThrow()
    }

    /**
     * Copies the folder to the given [destination].
     * Also renames the folder to name given in the path.
     */
    public suspend fun copy(
        destination: FolderPath,
        recursive: Boolean = true,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Folder> {
        return copy(destination, recursive, destination, duplicatesStrategy)
    }

    /**
     * Copies the folder to the given [destination].
     * If [recursive] is true child nodes will also be copied, otherwise only the folder itself is copied.
     */
    public suspend fun copy(
        destination: NodeParentPath,
        destinationId: String? = null,
        recursive: Boolean = true,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Folder> {
        return copy(destination.folder(destinationId ?: id), recursive, duplicatesStrategy)
    }

    /**
     * Copies the folder and all its child nodes to the given [parent].
     */
    override suspend fun copyInto(
        parent: NodeParentPath,
        id: String?,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Folder> {
        return copy(parent, id, true, duplicatesStrategy)
    }

    private suspend fun copy(
        destination: FolderPath,
        recursive: Boolean,
        skipPath: FolderPath,
        duplicatesStrategy: DuplicatesStrategy,
    ): Result<Folder> {
        // Check if the folder already exists
        tree[destination]?.let { folder ->
            when (duplicatesStrategy) {
                DuplicatesStrategy.FAIL -> return Result.failure(FolderAlreadyExistsException(folder.path))
                DuplicatesStrategy.SKIP -> return Result.success(folder)
                DuplicatesStrategy.REPLACE_EXISTING -> folder.delete()
            }
        }

        // Create the copy
        val folder = tree.create(destination, state)
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
            if (child.path.isSubpathOf(skipPath)) {
                continue
            }

            child.copy(parent.path.folder(child.id), true, skipPath, duplicatesStrategy).onFailure {
                return Result.failure(it)
            }
        }

        return Result.success(parent)
    }

    override suspend fun delete(): Result<FolderState> {
        return tree.delete(path)
    }
}
