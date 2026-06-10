package net.voxelpi.varp.tree

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.WarpState

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
        get() = tree.state[path]!!

    /**
     * the location of the warp.
     */
    public val location: MinecraftLocation
        get() = state.location

    /**
     * Modifies the state of the folder.
     */
    public suspend fun modify(state: WarpState): Result<Unit> {
        return tree.update(path, state)
    }

    /**
     * Modifies the state of the warp.
     */
    public suspend fun modify(init: WarpState.Builder.() -> Unit): Result<WarpState> = runCatching {
        val builder = WarpState.Builder(state)
        builder.init()
        val state = builder.build()
        modify(state).getOrThrow()
        state
    }

    /**
     * Moves the warp to the given [destination].
     * Also renames the warp to name given in the path.
     */
    public suspend fun move(
        destination: WarpPath,
    ): Result<Unit> = runCatching {
        tree.move(
            path,
            destination,
        ).getOrThrow()

        path = destination
    }

    override suspend fun moveInto(
        parent: NodeParentPath,
        id: String?,
    ): Result<Unit> = runCatching {
        move(parent.warp(id ?: this@Warp.id)).getOrThrow()
    }

    override suspend fun move(
        id: String,
    ): Result<Unit> = runCatching {
        move(path.parent.warp(id)).getOrThrow()
    }

    /**
     * Copies the warp to the given [destination].
     * Also renames the warp to the id given in the path.
     */
    public suspend fun copy(
        destination: WarpPath,
        duplicatesStrategy: DuplicatesStrategy = DuplicatesStrategy.FAIL,
    ): Result<Warp> = runCatching {
        // Check if the warp already exists
        tree[destination]?.let { warp ->
            when (duplicatesStrategy) {
                DuplicatesStrategy.FAIL -> throw WarpAlreadyExistsException(warp.path)
                DuplicatesStrategy.SKIP -> return@runCatching warp
                DuplicatesStrategy.REPLACE_EXISTING -> warp.delete()
            }
        }

        // Create the copy
        return tree.create(destination, state)
    }

    override suspend fun copyInto(
        parent: NodeParentPath,
        id: String?,
    ): Result<Warp> {
        return copy(parent.warp(id ?: this@Warp.id))
    }

    override suspend fun delete(): Result<WarpState> {
        return tree.delete(path)
    }
}
