package net.voxelpi.varp.mod.server.network

import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.exception.tree.WarpAlreadyExistsException
import net.voxelpi.varp.mod.network.VarpPacketRegistry
import net.voxelpi.varp.mod.network.protocol.VarpPacket
import net.voxelpi.varp.mod.network.protocol.clientbound.VarpClientboundPacket
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
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundPacket
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundTeleportWarpPacket
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpPermissions
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
        handleServerboundPacket(player, packet).getOrElse {
            server.logger.error("Unable to handle packet ${VarpPacket.packetId(packet::class)}. \"$message\"", it)
            return
        }
    }

    private fun handleServerboundPacket(player: VarpServerPlayerImpl, packet: VarpServerboundPacket): Result<Unit> {
        return runCatching {
            // TODO: Run on async thread.
            runBlocking {
                when (packet) {
                    is VarpServerboundCreateFolderPacket -> {
                        player.requirePermissionOrElse(VarpPermissions.FOLDER_CREATE) {
                            TODO("Messages")
                            return@runBlocking
                        }

                        val warp = server.tree.createFolder(packet.path, packet.state).getOrElse { exception ->
                            when (exception) {
                                is WarpAlreadyExistsException -> TODO("Messages")
                                else -> exception.printStackTrace()
                            }
                            return@runBlocking
                        }
                    }
                    is VarpServerboundCreateWarpPacket -> TODO()
                    is VarpServerboundDeleteFolderPacket -> TODO()
                    is VarpServerboundDeleteWarpPacket -> TODO()
                    is VarpServerboundInitializationPacket -> TODO()
                    is VarpServerboundModifyFolderPathPacket -> TODO()
                    is VarpServerboundModifyFolderStatePacket -> TODO()
                    is VarpServerboundModifyRootStatePacket -> TODO()
                    is VarpServerboundModifyWarpPathPacket -> TODO()
                    is VarpServerboundModifyWarpStatePacket -> TODO()
                    is VarpServerboundTeleportWarpPacket -> TODO()
                }
            }
        }
    }
}
