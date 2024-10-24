package net.voxelpi.varp.mod.fabric.server

import kotlinx.coroutines.cancel
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.server.network.FabricVarpServerNetworkHandler
import net.voxelpi.varp.mod.fabric.server.player.FabricVarpServerPlayerService
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.repository.filetree.FileTreeRepository

class FabricVarpServer(
    val server: MinecraftServer,
) : VarpServerImpl() {

    override val version: String
        get() = Varp.version

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val eventScope: EventScope = eventScope()

    override val loader: VarpLoader = VarpLoader.loader(server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("varp")) {
        registerRepositoryType<FileTreeRepository>()
    }

    override val serverNetworkHandler: FabricVarpServerNetworkHandler = FabricVarpServerNetworkHandler(this)

    init {
        loader.load()
        FabricVarpMod.logger.info("Loaded ${loader.repositories().size} repositories")
        FabricVarpMod.logger.info("Loaded ${compositor.mounts().size} mounts")
    }

    override val playerService: FabricVarpServerPlayerService = FabricVarpServerPlayerService(this)

    init {
        // Register api service
        VarpServerAPI.register(this)
    }

    fun cleanup() {
        playerService.handleShutdown()
        loader.save()
        coroutineScope.cancel()

        // Unregister api service.
        VarpServerAPI.unregister()
    }
}
