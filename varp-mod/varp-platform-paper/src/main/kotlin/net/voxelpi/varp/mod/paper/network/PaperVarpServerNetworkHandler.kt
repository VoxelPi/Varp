package net.voxelpi.varp.mod.paper.network

import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.player.PaperVarpServerPlayer
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class PaperVarpServerNetworkHandler(
    override val server: PaperVarpServer,
) : VarpServerNetworkHandler(server), PluginMessageListener {

    init {
        // Register plugin channels.
        server.server.messenger.registerOutgoingPluginChannel(server.plugin, VarpModConstants.VARP_PLUGIN_CHANNEL.asString())
        server.server.messenger.registerIncomingPluginChannel(server.plugin, VarpModConstants.VARP_PLUGIN_CHANNEL.asString(), this)
    }

    override fun sendClientboundPluginMessage(message: String, player: VarpServerPlayerImpl) {
        val buffer = message.toByteArray(Charsets.UTF_8)
        (player as PaperVarpServerPlayer).player.sendPluginMessage(server.plugin, VarpModConstants.VARP_PLUGIN_CHANNEL.asString(), buffer)
    }

    override fun onPluginMessageReceived(channel: String, paperPlayer: Player, data: ByteArray) {
        val message = data.toString(Charsets.UTF_8)
        val player = server.playerService.player(paperPlayer)
        handleServerboundPluginMessage(player, message)
    }
}
