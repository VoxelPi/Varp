package net.voxelpi.varp.tree

import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.state.FolderState

public class Root internal constructor(
    override val tree: Tree,
) : NodeParent {

    /**
     * The path of the root folder.
     */
    override val path: RootPath
        get() = RootPath

    /**
     * The state of the root folder.
     */
    override val state: FolderState
        get() = tree.rootState()

    /**
     * Modifies the state of the root folder.
     */
    override suspend fun modify(state: FolderState): Result<FolderState> {
        tree.rootState(state).getOrElse {
            return Result.failure(it)
        }
        return Result.success(state)
    }
}
