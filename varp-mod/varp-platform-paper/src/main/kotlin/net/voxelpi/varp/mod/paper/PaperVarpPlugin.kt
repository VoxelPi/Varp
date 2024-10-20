package net.voxelpi.varp.mod.paper

import net.voxelpi.varp.mod.paper.command.PaperVarpCommandService
import org.bukkit.plugin.java.JavaPlugin

class PaperVarpPlugin(
    val commandService: PaperVarpCommandService,
) : JavaPlugin() {

    lateinit var varpServer: PaperVarpServer
        private set

    override fun onEnable() {
        varpServer = PaperVarpServer(this, server)
    }

    override fun onDisable() {
        varpServer.cleanup()
    }
}
