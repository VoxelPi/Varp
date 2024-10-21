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
    override val id: String,
) : ClientRepository {

    override val registry: TreeStateRegistry = TreeStateRegistry()

    override var active: Boolean = false
        private set

    override fun reload(): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundInitializationPacket(client.version, VarpModConstants.PROTOCOL_VERSION))
        return Result.success(Unit)
    }

    override fun createWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateWarpPacket(path, state))
        return Result.success(Unit)
    }

    override fun createFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundCreateFolderPacket(path, state))
        return Result.success(Unit)
    }

    override fun saveWarpState(path: WarpPath, state: WarpState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpStatePacket(path, state))
        return Result.success(Unit)
    }

    override fun saveFolderState(path: FolderPath, state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderStatePacket(path, state))
        return Result.success(Unit)
    }

    override fun saveRootState(state: FolderState): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyRootStatePacket(state))
        return Result.success(Unit)
    }

    override fun deleteWarpState(path: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteWarpPacket(path))
        return Result.success(Unit)
    }

    override fun deleteFolderState(path: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundDeleteFolderPacket(path))
        return Result.success(Unit)
    }

    override fun moveWarpState(src: WarpPath, dst: WarpPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyWarpPathPacket(src, dst))
        return Result.success(Unit)
    }

    override fun moveFolderState(src: FolderPath, dst: FolderPath): Result<Unit> {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundModifyFolderPathPacket(src, dst))
        return Result.success(Unit)
    }

    // region packet handlers

    fun handlePacket(packet: VarpClientboundCreateFolderPacket) {
        registry[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundCreateWarpPacket) {
        registry[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundDeleteFolderPacket) {
        registry.remove(packet.path)
    }

    fun handlePacket(packet: VarpClientboundDeleteWarpPacket) {
        registry.remove(packet.path)
    }

    fun handlePacket(packet: VarpClientboundSyncTreePacket) {
        // Enable client support.
        client.logger.debug("Received state sync packet: ${packet.folders.size} folders, ${packet.warps.size} warps.")
        active = true

        // Update registry.
        registry.clear()
        registry.root = packet.root
        registry.folders.putAll(packet.folders)
        registry.warps.putAll(packet.warps)
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderPathPacket) {
        registry.move(packet.from, packet.to)
    }

    fun handlePacket(packet: VarpClientboundUpdateFolderStatePacket) {
        registry[packet.path] = packet.state
    }

    fun handlePacket(packet: VarpClientboundUpdateRootStatePacket) {
        registry.root = packet.state
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpPathPacket) {
        registry.move(packet.from, packet.to)
    }

    fun handlePacket(packet: VarpClientboundUpdateWarpStatePacket) {
        registry[packet.path] = packet.state
    }

    // endregion
}
