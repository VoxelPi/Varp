package net.voxelpi.varp.mod.server.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.voxelpi.varp.mod.network.VarpPacketRegistry
import net.voxelpi.varp.mod.network.protocol.VarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundPacket
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
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundTeleportWarpPacket
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl

abstract class VarpServerNetworkHandler(
    protected open val server: VarpServerImpl,
) {

    /**
     * Sends the given plugin message [message] to the given [player].
     */
    protected abstract fun sendClientboundPluginMessage(message: String, player: VarpServerPlayerImpl)

    /**
     *  Sends the given [packet] to the client of the given [player].
     */
    fun sendClientboundPacket(packet: VarpClientboundPacket, player: VarpServerPlayerImpl) {
        val message = VarpPacketRegistry.Clientbound.serializePacket(packet).getOrThrow()
        sendClientboundPluginMessage(message, player)
    }

    /**
     * Sends the given [packet] to the clients of all online players that have client support enabled.
     */
    fun sendClientboundPacketToAll(packet: VarpClientboundPacket) {
        val message = VarpPacketRegistry.Clientbound.serializePacket(packet).getOrThrow()
        for (player in server.playerService.players()) {
            sendClientboundPluginMessage(message, player)
        }
    }

    /**
     * Handles the given plugin message [message] received from the given [player].
     */
    protected fun handleServerboundPluginMessage(player: VarpServerPlayerImpl, message: String) {
        // Process the packet.
        val packet = VarpPacketRegistry.Serverbound.deserializePacket(message).getOrElse {
            server.logger.error("Unable to process packet. \"$message\"", it)
            return
        }

        // Handle the packet.
        server.coroutineScope.launch(Dispatchers.IO) {
            handleServerboundPacket(player, packet).getOrElse {
                server.logger.error("Unable to handle packet ${VarpPacket.packetId(packet::class)}. \"$message\"", it)
                return@launch
            }
        }
    }

    private suspend fun handleServerboundPacket(player: VarpServerPlayerImpl, packet: VarpServerboundPacket): Result<Unit> {
        return runCatching {
            when (packet) {
                is VarpServerboundCreateFolderPacket -> player.createFolder(packet.path, packet.state)
                is VarpServerboundCreateWarpPacket -> player.createWarp(packet.path, packet.state)
                is VarpServerboundDeleteFolderPacket -> player.deleteFolder(packet.path)
                is VarpServerboundDeleteWarpPacket -> player.deleteWarp(packet.path)
                is VarpServerboundClientInfoPacket -> player.enableClientSupport(packet.clientInformation())
                is VarpServerboundModifyFolderPathPacket -> player.moveFolder(packet.from, packet.to)
                is VarpServerboundModifyFolderStatePacket -> player.modifyFolder(packet.path, packet.state)
                is VarpServerboundModifyRootStatePacket -> player.modifyRoot(packet.state)
                is VarpServerboundModifyWarpPathPacket -> player.moveWarp(packet.from, packet.to)
                is VarpServerboundModifyWarpStatePacket -> player.modifyWarp(packet.path, packet.state)
                is VarpServerboundTeleportWarpPacket -> player.teleportToWarp(packet.path)
            }
        }
    }
}
