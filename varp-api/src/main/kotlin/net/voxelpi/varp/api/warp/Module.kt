package net.voxelpi.varp.api.warp

import net.voxelpi.varp.api.warp.path.ModulePath
import net.voxelpi.varp.api.warp.state.ModuleState
import net.voxelpi.varp.api.warp.tree.NodeParent

interface Module : NodeParent {

    /**
     * The path to the module.
     */
    override val path: ModulePath

    /**
     * The state of the module.
     */
    override var state: ModuleState

    /**
     * Modifies the state of the module.
     */
    fun modify(init: ModuleState.Builder.() -> Unit): ModuleState {
        val builder = ModuleState.Builder(this.state)
        builder.init()
        this.state = builder.build()
        return this.state
    }
}
