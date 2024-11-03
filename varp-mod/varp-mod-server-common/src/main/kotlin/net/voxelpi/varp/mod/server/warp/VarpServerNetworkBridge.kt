package net.voxelpi.varp.mod.server.warp

import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundSyncTreePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateRootStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpStatePacket
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.warp.Tree

class VarpServerNetworkBridge(
    private val tree: Tree,
    private val serverNetworkHandler: VarpServerNetworkHandler,
) {

    init {
        tree.eventScope.registerAnnotated(this)
    }

    fun cleanup() {
        tree.eventScope.unregisterAnnotated(this)
    }

    @Subscribe
    fun handle(event: RepositoryLoadEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundSyncTreePacket(tree))
    }

    @Subscribe
    fun handle(event: FolderCreateEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundCreateFolderPacket(event.folder.path, event.folder.state))
    }

    @Subscribe
    fun handle(event: WarpCreateEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundCreateWarpPacket(event.warp.path, event.warp.state))
    }

    @Subscribe
    fun handle(event: FolderDeleteEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundDeleteFolderPacket(event.folder.path))
    }

    @Subscribe
    fun handle(event: WarpDeleteEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundDeleteWarpPacket(event.warp.path))
    }

    @Subscribe
    fun handle(event: FolderPathChangeEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundUpdateFolderPathPacket(event.oldPath, event.newPath))
    }

    @Subscribe
    fun handle(event: WarpPathChangeEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundUpdateWarpPathPacket(event.oldPath, event.newPath))
    }

    @Subscribe
    fun handle(event: RootStateChangeEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundUpdateRootStatePacket(event.root.state))
    }

    @Subscribe
    fun handle(event: FolderStateChangeEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundUpdateFolderStatePacket(event.folder.path, event.folder.state))
    }

    @Subscribe
    fun handle(event: WarpStateChangeEvent) {
        serverNetworkHandler.sendClientboundPacketToAll(VarpClientboundUpdateWarpStatePacket(event.warp.path, event.warp.state))
    }
}
