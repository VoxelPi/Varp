package net.voxelpi.varp.warp

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.warp.node.NodeChild
import net.voxelpi.varp.warp.path.NodeChildPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.WarpState

/**
 * @property path the path to the warp.
 */
public class Warp internal constructor(
    override val tree: Tree,
    override val path: WarpPath,
) : NodeChild {

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
        val builder = WarpState.Builder(this.state)
        builder.init()
        this.state = builder.build()
        return this.state
    }

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Warp> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Warp> {
        TODO("Not yet implemented")
    }

    override fun delete(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }
}
