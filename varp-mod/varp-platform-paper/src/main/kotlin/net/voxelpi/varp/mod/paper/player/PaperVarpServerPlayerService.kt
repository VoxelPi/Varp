package net.voxelpi.varp.mod.paper.player

import net.voxelpi.varp.mod.paper.PaperVarpServer
import net.voxelpi.varp.mod.paper.util.varpLocation
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PaperVarpServerPlayerService(
    override val server: PaperVarpServer,
) : VarpServerPlayerServiceImpl<PaperVarpServerPlayer>(server), Listener {

    init {
        server.server.pluginManager.registerEvents(this, server.plugin)
    }

    fun player(player: Player): PaperVarpServerPlayer {
        return players[player.uniqueId]!!
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onLogin(event: PlayerJoinEvent) {
        val player = PaperVarpServerPlayer(server, event.player)
        players[player.uniqueId] = player
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onQuit(event: PlayerQuitEvent) {
        val player = player(event.player)
        players.remove(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        val player = player(event.player)
        val from = event.from.varpLocation()
        val to = event.to.varpLocation()
        player.handleTeleport(from, to)
    }
}
