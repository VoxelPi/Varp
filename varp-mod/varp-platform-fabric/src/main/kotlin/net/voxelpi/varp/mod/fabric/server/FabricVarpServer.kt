package net.voxelpi.varp.mod.fabric.server

import net.minecraft.server.MinecraftServer
import net.minecraft.util.WorldSavePath
import net.voxelpi.varp.Varp
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.repository.filetree.FileTreeRepository

class FabricVarpServer(
    val server: MinecraftServer,
) : VarpServerAPI {

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

    init {
        // Register api service
        VarpServerAPI.register(this)
    }

    fun cleanup() {
        loader.save()

        // Unregister api service.
        VarpServerAPI.unregister()
    }
}
