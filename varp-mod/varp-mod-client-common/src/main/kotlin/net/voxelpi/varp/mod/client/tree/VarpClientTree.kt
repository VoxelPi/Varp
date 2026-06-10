package net.voxelpi.varp.mod.client.tree

import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.event.post
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.api.tree.ClientTree
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
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
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyRootStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpStatePacket
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.MutableTreeState
import net.voxelpi.varp.tree.state.TreeState
import net.voxelpi.varp.tree.state.WarpState

class VarpClientTree(
    private val client: VarpClientImpl,
    private val clientNetworkHandler: VarpClientNetworkHandler,
) : ClientTree {

    override val state: TreeState
        field = MutableTreeState()

    override val eventScope: EventScope = eventScope()

    override suspend fun create(path: WarpPath, state: WarpState): Result<Warp> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateWarpPacket(path, state))

        // TODO: TEMP FIX.
        this.state[path] = state
        return Result.success(this[path]!!)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Folder> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateFolderPacket(path, state))

        // TODO: TEMP FIX.
        this.state[path] = state
        return Result.success(this[path]!!)
    }

    override suspend fun delete(path: WarpPath): Result<WarpState> {
        val previousState = state[path] ?: return Result.failure(WarpNotFoundException(path))
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteWarpPacket(path))
        return Result.success(previousState)
    }

    override suspend fun delete(path: FolderPath): Result<FolderState> {
        val previousState = state[path] ?: return Result.failure(FolderNotFoundException(path))
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteFolderPacket(path))
        return Result.success(previousState)
    }

    override suspend fun update(path: WarpPath, newState: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpStatePacket(path, newState))
        return Result.success(Unit)
    }

    override suspend fun update(path: FolderPath, newState: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderStatePacket(path, newState))
        return Result.success(Unit)
    }

    override suspend fun update(path: RootPath, newState: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyRootStatePacket(newState))
        return Result.success(Unit)
    }

    override suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpPathPacket(src, dst))
        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderPathPacket(src, dst))
        return Result.success(Unit)
    }

    fun handlePacket(packet: VarpClientboundCreateFolderPacket) {
        // Update state.
        state[packet.path] = packet.state

        // Post event.
        eventScope.post(FolderCreateEvent(this[packet.path]!!))
    }

    fun handlePacket(packet: VarpClientboundCreateWarpPacket) {
        // Update state.
        state[packet.path] = packet.state

        // Post event.
        eventScope.post(WarpCreateEvent(this[packet.path]!!))
    }

    fun handlePacket(packet: VarpClientboundDeleteFolderPacket) {
        // Post event.
        this[packet.path]?.let { folder ->
            eventScope.post(FolderDeleteEvent(folder))
        }

        // Update state.
        val previousState = state.delete(packet.path)

        // Post event.
        if (previousState != null) {
            eventScope.post(FolderPostDeleteEvent(packet.path, previousState))
        }
    }

    fun handlePacket(packet: VarpClientboundDeleteWarpPacket) {
        // Post event.
        this[packet.path]?.let { warp ->
            eventScope.post(WarpDeleteEvent(warp))
        }

        // Update state.
        val state = state.delete(packet.path)

        // Post event.
        if (state != null) {
            eventScope.post(WarpPostDeleteEvent(packet.path, state))
        }
    }

    fun handlePacket(packet: VarpClientboundSyncTreePacket) {
        // Enable client support.
        client.logger.debug("Received state sync packet: ${packet.folders.size} folders, ${packet.warps.size} warps.")

        // Update state.
        state.update(MutableTreeState(packet.warps.toMutableMap(), packet.folders.toMutableMap(), packet.root))

        // Post load event
        // TODO: New event. Maybe TreeUpdateEvent?
        // eventScope.post(RepositoryLoadEvent(this))
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderPathPacket) {
        // Update state.
        state.move(packet.from, packet.to)

        // Post event.
        eventScope.post(FolderPathChangeEvent(this[packet.to]!!, packet.to, packet.from))
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderStatePacket) {
        // Temporary save previous state.
        val previousState = state[packet.path]

        // Update state.
        state[packet.path] = packet.state

        // Post event.
        if (previousState != null) {
            eventScope.post(FolderStateChangeEvent(this[packet.path]!!, packet.state, previousState))
        }
    }

    fun handlePacket(packet: VarpClientboundUpdateRootStatePacket) {
        // Temporary save previous state.
        val previousState = state.root

        // Update state.
        state.root = packet.state

        // Post event.
        eventScope.post(RootStateChangeEvent(root, packet.state, previousState))
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpPathPacket) {
        // Update state.
        state.move(packet.from, packet.to)

        // Post event.
        eventScope.post(WarpPathChangeEvent(this[packet.to]!!, packet.to, packet.from))
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpStatePacket) {
        // Temporary save previous state.
        val previousState = state[packet.path]

        // Update state.
        state[packet.path] = packet.state

        // Post event.
        if (previousState != null) {
            eventScope.post(WarpStateChangeEvent(this[packet.path]!!, packet.state, previousState))
        }
    }

    internal fun reset() {
        state.clear()
    }
}
