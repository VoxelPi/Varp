package net.voxelpi.varp.warp

import net.voxelpi.varp.api.DuplicatesStrategy
import net.voxelpi.varp.api.warp.Warp
import net.voxelpi.varp.api.warp.path.NodeChildPath
import net.voxelpi.varp.api.warp.path.NodeParentPath
import net.voxelpi.varp.api.warp.path.WarpPath
import net.voxelpi.varp.api.warp.state.WarpState
import net.voxelpi.varp.warp.tree.VarpNodeChild

class VarpWarp(
    override val tree: VarpTree,
    override val path: WarpPath,
) : Warp, VarpNodeChild {

    override var state: WarpState
        get() = tree.warpState(path)!!
        set(value) = tree.warpState(path, value).getOrThrow()

    override fun move(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeParentPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpWarp> {
        TODO("Not yet implemented")
    }

    override fun copy(destination: NodeChildPath, duplicatesStrategy: DuplicatesStrategy): Result<VarpWarp> {
        TODO("Not yet implemented")
    }

    override fun delete(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun move(id: String, duplicatesStrategy: DuplicatesStrategy): Result<Unit> {
        TODO("Not yet implemented")
    }
}
