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
    override var state: WarpState
        get() = tree.warpState(path)!!
        set(value) = tree.warpState(path, value).getOrThrow()

    /**
     * Modifies the state of the warp.
     */
    public fun modify(init: WarpState.Builder.() -> Unit): WarpState {
        val builder = WarpState.Builder(state)
        builder.init()
        state = builder.build()
        return state
    }

    /**
     * Moves the warp to the given [destination].
     * Also renames the warp to name given in the path.
     */
    public fun move(destination: WarpPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        tree.move(path, destination, duplicatesStrategy).onFailure {
            return Result.failure(it)
        }

        path = destination
        return Result.success(Unit)
    }

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy, destinationId: String?): Result<Unit> {
        return move(destination.warp(destinationId ?: id), duplicatesStrategy)
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        return move(path.parent.warp(id), duplicatesStrategy)
    }

    /**
     * Copies the warp to the given [destination].
     * Also renames the warp to the id given in the path.
     */
    public fun copy(destination: WarpPath, duplicatesStrategy: DuplicatesStrategy): Result<Warp> {
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

    override fun copy(
        destination: NodeParentPath,
        duplicatesStrategy: DuplicatesStrategy,
        destinationId: String?,
    ): Result<Warp> {
        return copy(destination.warp(destinationId ?: id), duplicatesStrategy)
    }

    override fun delete(): Result<Unit> {
        return tree.deleteWarp(path)
    }
}
