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
    override var state: FolderState
        get() = tree.rootState()
        set(value) = tree.rootState(value).getOrThrow()

    /**
     * Modifies the state of the root folder.
     */
    public fun modify(init: FolderState.Builder.() -> Unit): FolderState {
        val builder = FolderState.Builder(state)
        builder.init()
        state = builder.build()
        return state
    }
}
