package net.voxelpi.varp.mod.network.protocol.clientbound

import net.voxelpi.varp.mod.network.protocol.PacketId
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.state.TreeState

/**
 * Send by the server to the client to update the state of the tree.
 * @property state The state of the tree.
 */
@JvmRecord
@PacketId("sync_tree")
data class VarpClientboundSyncTreePacket(
    val state: TreeState,
) : VarpClientboundPacket {

    constructor(tree: Tree) : this(tree.state)
}
