package net.voxelpi.varp.mod.client.warp

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
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundInitializationPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyRootStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundModifyWarpStatePacket
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.WarpPath
import net.voxelpi.varp.warp.state.FolderState
import net.voxelpi.varp.warp.state.TreeStateRegistry
import net.voxelpi.varp.warp.state.WarpState

class ClientRepositoryImpl(
    private val client: VarpClientImpl,
    private val clientNetworkHandler: VarpClientNetworkHandler,
    id: String,
) : ClientRepository(id) {

    override val registryView: TreeStateRegistry = TreeStateRegistry()

    override var active: Boolean = false
        private set

    override suspend fun load(): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundInitializationPacket(client.version, VarpModConstants.PROTOCOL_VERSION))
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

    override suspend fun move(src: WarpPath, dst: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpPathPacket(src, dst))
        return Result.success(Unit)
    }

    override suspend fun move(src: FolderPath, dst: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderPathPacket(src, dst))
        return Result.success(Unit)
    }

    // region packet handlers

    fun handlePacket(packet: VarpClientboundCreateFolderPacket) {
        registryView[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundCreateWarpPacket) {
        registryView[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundDeleteFolderPacket) {
        registryView.delete(packet.path)
    }

    fun handlePacket(packet: VarpClientboundDeleteWarpPacket) {
        registryView.delete(packet.path)
    }

    fun handlePacket(packet: VarpClientboundSyncTreePacket) {
        // Enable client support.
        client.logger.debug("Received state sync packet: ${packet.folders.size} folders, ${packet.warps.size} warps.")
        active = true

        // Update registry.
        registryView.clear()
        registryView.root = packet.root
        registryView.folders.putAll(packet.folders)
        registryView.warps.putAll(packet.warps)
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderPathPacket) {
        registryView.move(packet.from, packet.to)
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderStatePacket) {
        registryView[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundUpdateRootStatePacket) {
        registryView.root = packet.state
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpPathPacket) {
        registryView.move(packet.from, packet.to)
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpStatePacket) {
        registryView[packet.path] = packet.state
    }

    // endregion
}
