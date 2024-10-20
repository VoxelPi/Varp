package net.voxelpi.varp.mod.paper

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.paper.network.PaperVarpServerNetworkHandler
import net.voxelpi.varp.mod.paper.player.PaperVarpServerPlayerService
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.repository.filetree.FileTreeRepository
import org.bukkit.Server
import org.bukkit.plugin.ServicePriority

class PaperVarpServer(
    val plugin: PaperVarpPlugin,
    val server: Server,
) : VarpServerImpl {

    override val version: String
        get() = Varp.version

    override val logger: ComponentLogger
        get() = plugin.componentLogger

    override val eventScope: EventScope = eventScope()

    override val loader: VarpLoader = VarpLoader.loader(plugin.dataPath.resolve("data")) {
        registerRepositoryType<FileTreeRepository>()
    }

    override val serverNetworkHandler: PaperVarpServerNetworkHandler = PaperVarpServerNetworkHandler(this)

    init {
        loader.load()
        plugin.componentLogger.info("Loaded ${loader.repositories().size} repositories")
        plugin.componentLogger.info("Loaded ${compositor.mounts().size} mounts")
    }

    override val playerService: PaperVarpServerPlayerService = PaperVarpServerPlayerService(this)

    init {
        // Register api service
        plugin.server.servicesManager.register(VarpServerAPI::class.java, this, plugin, ServicePriority.Normal)
        VarpServerAPI.register(this)
    }

    fun cleanup() {
        loader.save()

        // Unregister api service.
        VarpServerAPI.unregister()
    }
}
