package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.tree.NodeChild
import net.voxelpi.varp.api.warp.tree.NodeParent

interface Folder : NodeChild, NodeParent {

    /**
     * The path to the folder.
     */
    override val path: FolderPath

    /**
     * The state of the folder.
     */
    override var state: FolderState

    /**
     * Modifies the state of the folder.
     */
    fun modify(init: FolderState.Builder.() -> Unit): FolderState {
        val builder = FolderState.Builder(this.state)
        builder.init()
        this.state = builder.build()
        return this.state
    }
}
