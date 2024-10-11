package net.voxelpi.varp.warp

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.Folder
import net.voxelpi.varp.api.warp.path.FolderPath
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.warp.tree.VarpNodeChild
import net.voxelpi.varp.warp.tree.VarpNodeParent

class VarpFolder(
    override val tree: VarpTree,
    override val path: FolderPath,
) : Folder, VarpNodeChild, VarpNodeParent {

    override var state: FolderState
        get() = tree.folderState(path)!!
        set(value) = tree.folderState(path, value).getOrThrow()

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun delete(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpFolder> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpFolder> {
        TODO("Not yet implemented")
    }
}
