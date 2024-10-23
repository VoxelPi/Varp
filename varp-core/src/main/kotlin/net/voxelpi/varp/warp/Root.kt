package net.voxelpi.varp.warp

import net.voxelpi.varp.warp.path.RootPath
import net.voxelpi.varp.warp.state.FolderState

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
    public suspend fun modify(state: FolderState): Result<FolderState> {
        tree.rootState(state).getOrElse {
            return Result.failure(it)
        }
        return Result.success(state)
    }

    /**
     * Modifies the state of the root folder.
     */
    public suspend fun modify(init: FolderState.Builder.() -> Unit): Result<FolderState> {
        val builder = FolderState.Builder(state)
        builder.init()
        return modify(builder.build())
    }
}
