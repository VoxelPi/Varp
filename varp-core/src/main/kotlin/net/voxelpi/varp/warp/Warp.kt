package net.voxelpi.varp.warp

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.WarpState

public class Warp internal constructor(
    override val tree: Tree,
    path: WarpPath,
) : NodeChild {

    /**
     * The path to the warp.
     */
    override var path: WarpPath = path
        private set

    /**
     * The state of the warp.
     */
    override val state: WarpState
        get() = tree.warpState(path)!!

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(state: WarpState): Result<WarpState> {
        tree.warpState(path, state).getOrElse {
            return Result.failure(it)
        }
        return Result.success(state)
    }

    /**
     * Modifies the state of the warp.
     */
    public suspend fun modify(init: WarpState.Builder.() -> Unit): Result<WarpState> {
        val builder = WarpState.Builder(state)
        builder.init()
        return modify(builder.build())
    }

    /**
     * Moves the warp to the given [destination].
     * Also renames the warp to name given in the path.
     */
    public suspend fun move(destination: WarpPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        tree.move(path, destination, duplicatesStrategy).onFailure {
            return Result.failure(it)
        }

        path = destination
        return Result.success(Unit)
    }

    override suspend fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String?): Result<Unit> {
        return move(destination.warp(destinationId ?: id), duplicatesStrategy)
    }

    override suspend fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        return move(path.parent.warp(id), duplicatesStrategy)
    }

    /**
     * Copies the warp to the given [destination].
     * Also renames the warp to the id given in the path.
     */
    public suspend fun copy(destination: WarpPath, duplicatesStrategy: DuplicatesStrategy): Result<Warp> {
        // Check if the warp already exists
        tree.resolve(destination)?.let { warp ->
            when (duplicatesStrategy) {
                DuplicatesStrategy.FAIL -> return Result.failure(WarpAlreadyExistsException(warp.path))
                DuplicatesStrategy.SKIP -> return Result.success(warp)
                DuplicatesStrategy.REPLACE_EXISTING -> warp.delete()
            }
        }

        // Create the copy
        return tree.createWarp(destination, state)
    }

    override suspend fun copy(
        destination: NodeParentPath,
        duplicatesStrategy: DuplicatesStrategy,
        destinationId: String?,
    ): Result<Warp> {
        return copy(destination.warp(destinationId ?: id), duplicatesStrategy)
    }

    override suspend fun delete(): Result<Unit> {
        return tree.deleteWarp(path)
    }
}
