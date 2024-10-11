package net.voxelpi.varp.warp.tree

import net.voxelpi.varp.api.warp.node.NodeParent
import net.voxelpi.varp.api.warp.state.FolderState
import net.voxelpi.varp.api.warp.state.WarpState
import net.voxelpi.varp.warp.VarpFolder
import net.voxelpi.varp.warp.VarpWarp

interface VarpNodeParent : NodeParent, VarpNode {

    override fun childWarps(): Collection<VarpWarp> {
        return tree.warps(path, false)
    }

    override fun childFolders(): Collection<VarpFolder> {
        return tree.folders(path, false)
    }

    override fun hasChildWarp(id: String): Boolean {
        return childWarps().any { it.id == id }
    }

    override fun hasChildFolder(id: String): Boolean {
        return childFolders().any { it.id == id }
    }

    override fun childWarp(id: String): VarpWarp? {
        return tree.warp(path.warp(id))
    }

    override fun childFolder(id: String): VarpFolder? {
        return tree.folder(path.folder(id))
    }

    override fun createWarp(id: String, state: WarpState): Result<VarpWarp> {
        val path = this.path.warp(id)
        return tree.createWarp(path, state)
    }

    override fun createFolder(id: String, state: FolderState): Result<VarpFolder> {
        val path = this.path.folder(id)
        return tree.createFolder(path, state)
    }
}
