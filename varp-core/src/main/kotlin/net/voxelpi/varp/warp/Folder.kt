package net.voxelpi.varp.warp

import net.voxelpi.varp.DuplicatesStrategy
import net.voxelpi.varp.warp.node.NodeChild
import net.voxelpi.varp.warp.node.NodeParent
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeChildPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.state.FolderState

/**
 * @property path the path to the folder.
 */
public class Folder internal constructor(
    override val tree: Tree,
    override val path: FolderPath,
) : NodeChild, NodeParent {

    /**
     * The state of the folder.
     */
    override var state: FolderState
        get() = tree.folderState(path)!!
        set(value) = tree.folderState(path, value).getOrThrow()

    /**
     * Modifies the state of the folder.
     */
    public fun modify(init: FolderState.Builder.() -> Unit): FolderState {
        val builder = FolderState.Builder(this.state)
        builder.init()
        this.state = builder.build()
        return this.state
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Folder> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Folder> {
        TODO("Not yet implemented")
    }

    override fun delete(): Result<Unit> {
        TODO("Not yet implemented")
    }
}
