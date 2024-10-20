package net.voxelpi.varp.mod.fabric.server

import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.voxelpi.varp.Varp
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.server.player.FabricVarpServerPlayerService
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.repository.filetree.FileTreeRepository

class FabricVarpServer(
    val server: MinecraftServer,
) : VarpServerImpl {

    override val version: String
        get() = Varp.version

    override val loader: VarpLoader = VarpLoader.loader(server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("varp")) {
        registerRepositoryType<FileTreeRepository>()
    }

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

        // Unregister api service.
        VarpServerAPI.unregister()
    }
}
