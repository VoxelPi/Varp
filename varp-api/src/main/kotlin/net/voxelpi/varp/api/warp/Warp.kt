package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.node.NodeChild
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.WarpState

interface Warp : NodeChild {

    /**
     * The path to the warp.
     */
    override val path: WarpPath

    /**
     * The state of the warp.
     */
    override var state: WarpState

    /**
     * Modifies the state of the warp.
     */
    fun modify(init: WarpState.Builder.() -> Unit): WarpState {
        val builder = WarpState.Builder(this.state)
        builder.init()
        this.state = builder.build()
        return this.state
    }
}
