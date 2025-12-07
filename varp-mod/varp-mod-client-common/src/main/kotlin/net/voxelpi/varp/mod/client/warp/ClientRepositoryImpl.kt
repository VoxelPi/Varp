package net.voxelpi.varp.mod.client.warp

import net.voxelpi.event.post
import net.voxelpi.varp.event.folder.FolderCreateEvent
import net.voxelpi.varp.event.folder.FolderDeleteEvent
import net.voxelpi.varp.event.folder.FolderPathChangeEvent
import net.voxelpi.varp.event.folder.FolderPostDeleteEvent
import net.voxelpi.varp.event.folder.FolderStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.event.root.RootStateChangeEvent
import net.voxelpi.varp.event.warp.WarpCreateEvent
import net.voxelpi.varp.event.warp.WarpDeleteEvent
import net.voxelpi.varp.event.warp.WarpPathChangeEvent
import net.voxelpi.varp.event.warp.WarpPostDeleteEvent
import net.voxelpi.varp.event.warp.WarpStateChangeEvent
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.api.warp.ClientRepository
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
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundClientInfoPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyRootStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpStatePacket
import net.voxelpi.varp.option.OptionsContext
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.WarpPath
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.TreeStateRegistry
import net.voxelpi.varp.tree.state.TreeStateRegistryView
import net.voxelpi.varp.tree.state.WarpState

class ClientRepositoryImpl(
    private val client: VarpClientImpl,
    private val clientNetworkHandler: VarpClientNetworkHandler,
    id: String,
) : ClientRepository(id) {

    private val registry: TreeStateRegistry = TreeStateRegistry()

    override val registryView: TreeStateRegistryView
        get() = registry

    override val active: Boolean
        get() = client.serverInfo != null

    override suspend fun load(): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundClientInfoPacket(client.version, VarpModConstants.PROTOCOL_VERSION))
        return Result.success(Unit)
    }

    override suspend fun create(path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateWarpPacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun create(path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateFolderPacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun save(path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpStatePacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun save(path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderStatePacket(path, state))
        return Result.success(Unit)
    }

    override suspend fun save(state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyRootStatePacket(state))
        return Result.success(Unit)
    }

    override suspend fun delete(path: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteWarpPacket(path))
        return Result.success(Unit)
    }

    override suspend fun delete(path: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteFolderPacket(path))
        return Result.success(Unit)
    }

    override suspend fun move(src: WarpPath, dst: WarpPath, options: OptionsContext): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpPathPacket(src, dst))
        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath, options: OptionsContext): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderPathPacket(src, dst))
        return Result.success(Unit)
    }

    // region packet handlers

    fun handlePacket(packet: VarpClientboundCreateFolderPacket) {
        // Update registry.
        registry[packet.path] = packet.state

        // Post event.
        tree.eventScope.post(FolderCreateEvent(tree.resolve(packet.path)!!))
    }

    fun handlePacket(packet: VarpClientboundCreateWarpPacket) {
        // Update registry.
        registry[packet.path] = packet.state

        // Post event.
        tree.eventScope.post(WarpCreateEvent(tree.resolve(packet.path)!!))
    }

    fun handlePacket(packet: VarpClientboundDeleteFolderPacket) {
        // Post event.
        tree.resolve(packet.path)?.let { folder ->
            tree.eventScope.post(FolderDeleteEvent(folder))
        }

        // Update registry.
        val state = registry.delete(packet.path)

        // Post event.
        if (state != null) {
            tree.eventScope.post(FolderPostDeleteEvent(packet.path, state))
        }
    }

    fun handlePacket(packet: VarpClientboundDeleteWarpPacket) {
        // Post event.
        tree.resolve(packet.path)?.let { warp ->
            tree.eventScope.post(WarpDeleteEvent(warp))
        }

        // Update registry.
        val state = registry.delete(packet.path)

        // Post event.
        if (state != null) {
            tree.eventScope.post(WarpPostDeleteEvent(packet.path, state))
        }
    }

    fun handlePacket(packet: VarpClientboundSyncTreePacket) {
        // Enable client support.
        client.logger.debug("Received state sync packet: ${packet.folders.size} folders, ${packet.warps.size} warps.")

        // Update registry.
        registry.clear()
        registry.root = packet.root
        registry.folders.putAll(packet.folders)
        registry.warps.putAll(packet.warps)

        // Post load event
        tree.eventScope.post(RepositoryLoadEvent(this))
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderPathPacket) {
        // Update registry.
        registry.move(packet.from, packet.to)

        // Post event.
        tree.eventScope.post(FolderPathChangeEvent(tree.resolve(packet.to)!!, packet.to, packet.from))
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderStatePacket) {
        // Temporary save previous state.
        val previousState = registry[packet.path]

        // Update registry.
        registry[packet.path] = packet.state

        // Post event.
        if (previousState != null) {
            tree.eventScope.post(FolderStateChangeEvent(tree.resolve(packet.path)!!, packet.state, previousState))
        }
    }

    fun handlePacket(packet: VarpClientboundUpdateRootStatePacket) {
        // Temporary save previous state.
        val previousState = registryView.root

        // Update registry.
        registry.root = packet.state

        // Post event.
        tree.eventScope.post(RootStateChangeEvent(tree.root, packet.state, previousState))
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpPathPacket) {
        // Update registry.
        registry.move(packet.from, packet.to)

        // Post event.
        tree.eventScope.post(WarpPathChangeEvent(tree.resolve(packet.to)!!, packet.to, packet.from))
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpStatePacket) {
        // Temporary save previous state.
        val previousState = registry[packet.path]

        // Update registry.
        registry[packet.path] = packet.state

        // Post event.
        if (previousState != null) {
            tree.eventScope.post(WarpStateChangeEvent(tree.resolve(packet.path)!!, packet.state, previousState))
        }
    }

    // endregion

    fun reset() {
        registry.clear()
    }
}
