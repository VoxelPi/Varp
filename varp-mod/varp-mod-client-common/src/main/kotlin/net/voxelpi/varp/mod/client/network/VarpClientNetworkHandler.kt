package net.voxelpi.varp.mod.client.network

import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.network.VarpPacketRegistry
import net.voxelpi.varp.mod.network.protocol.VarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundCreateWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteFolderPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundDeleteWarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundOpenExplorerPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundServerInfoPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundSyncTreePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateFolderStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateRootStatePacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpPathPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundUpdateWarpStatePacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundPacket

abstract class VarpClientNetworkHandler(
    protected open val client: VarpClientImpl,
) {

    /**
     * Sends the given plugin message [message] to the server.
     */
    protected abstract fun sendServerboundPluginMessage(message: String)

    /**
     * Sends the given [packet] to the server.
     */
    fun sendServerboundPacket(packet: VarpServerboundPacket) {
        val message = VarpPacketRegistry.Serverbound.serializePacket(packet).getOrThrow()
        sendServerboundPluginMessage(message)
    }

    protected fun handleClientboundPluginMessage(message: String) {
        // Process the packet.
        val packet = VarpPacketRegistry.Clientbound.deserializePacket(message).getOrElse {
            client.logger.error("Unable to process packet. \"$message\"", it)
            return
        }

        // Handle the packet.
        handleClientboundPacket(packet).getOrElse {
            client.logger.error("Unable to handle packet ${VarpPacket.packetId(packet::class)}. \"$message\"", it)
            return
        }
    }

    private fun handleClientboundPacket(packet: VarpClientboundPacket): Result<Unit> {
        return runCatching {
            when (packet) {
                is VarpClientboundCreateFolderPacket -> client.repository.handlePacket(packet)
                is VarpClientboundCreateWarpPacket -> client.repository.handlePacket(packet)
                is VarpClientboundDeleteFolderPacket -> client.repository.handlePacket(packet)
                is VarpClientboundDeleteWarpPacket -> client.repository.handlePacket(packet)
                is VarpClientboundOpenExplorerPacket -> client.openExplorer(packet.path)
                is VarpClientboundServerInfoPacket -> client.enableBridge(packet.serverInformation())
                is VarpClientboundSyncTreePacket -> client.repository.handlePacket(packet)
                is VarpClientboundUpdateFolderPathPacket -> client.repository.handlePacket(packet)
                is VarpClientboundUpdateFolderStatePacket -> client.repository.handlePacket(packet)
                is VarpClientboundUpdateRootStatePacket -> client.repository.handlePacket(packet)
                is VarpClientboundUpdateWarpPathPacket -> client.repository.handlePacket(packet)
                is VarpClientboundUpdateWarpStatePacket -> client.repository.handlePacket(packet)
            }
        }
    }
}
