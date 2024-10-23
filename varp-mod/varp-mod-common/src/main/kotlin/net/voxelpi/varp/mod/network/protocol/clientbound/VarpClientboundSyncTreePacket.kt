package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistryView
import net.voxelpi.varp.warp.state.WarpState

/**
 * Send by the server to the client to update the state of the root and all loaded warps and folders.
 * @property root The state of the root.
 * @property folders A map containing the state of all folders.
 * @property warps A map containing the state of all clients.
 */
@JvmRecord
@PacketId("sync_tree")
data class VarpClientboundSyncTreePacket(
    val root: FolderState,
    val folders: Map<FolderPath, FolderState>,
    val warps: Map<WarpPath, WarpState>,
) : VarpClientboundPacket {

    constructor(treeStateRegistry: TreeStateRegistryView) : this(treeStateRegistry.root, treeStateRegistry.folders, treeStateRegistry.warps)

    constructor(tree: Tree) : this(tree.repository.registryView)
}
