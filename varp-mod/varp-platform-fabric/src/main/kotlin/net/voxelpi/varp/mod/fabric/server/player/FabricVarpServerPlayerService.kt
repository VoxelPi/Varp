package net.voxelpi.varp.mod.fabric.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl

class FabricVarpServerPlayerService(
    override val server: FabricVarpServer,
) : VarpServerPlayerServiceImpl<FabricVarpServerPlayer>(server) {

    fun player(player: ServerPlayer): FabricVarpServerPlayer {
        return players[player.uuid]!!
    }

    fun handleJoin(handler: ServerGamePacketListenerImpl) {
        val player = FabricVarpServerPlayer(server, handler.player)
        players[player.uniqueId] = player
    }

    fun handleQuit(handler: ServerGamePacketListenerImpl) {
        val player = player(handler.player)
        players.remove(player.uniqueId)
    }

    fun handleShutdown() {
        players.clear()
    }
}
