package net.voxelpi.varp.mod.fabric.server

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.WorldSavePath
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.server.network.FabricVarpServerNetworkHandler
import net.voxelpi.varp.mod.fabric.server.player.FabricVarpServerPlayerService
import net.voxelpi.varp.mod.fabric.util.toIdentifier
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.repository.filetree.FileTreeRepository
import java.nio.file.Path

class FabricVarpServer(
    val server: MinecraftServer,
) : VarpServerImpl() {

    override val version: String
        get() = Varp.version

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val globalConfigDirectory: Path
        get() = FabricVarpMod.configDirectory

    override val eventScope: EventScope = eventScope()

    override val platform: FabricServerPlatform = FabricServerPlatform()

    init {
        loadMessages()
    }

    override val loader: VarpLoader = VarpLoader.loader(server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve("varp")) {
        registerRepositoryType<FileTreeRepository>()
    }

    override val serverNetworkHandler: FabricVarpServerNetworkHandler = FabricVarpServerNetworkHandler(this)

    init {
        runBlocking {
            loader.load()
        }
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
        runBlocking {
            loader.save()
            loader.cleanup()
        }
        coroutineScope.cancel()

        // Unregister api service.
        VarpServerAPI.unregister()
    }

    override fun copyResourceTemplate(resource: String, destination: Path) {
        FabricVarpMod.copyResourceTemplate(resource, destination)
    }

    fun world(key: Key): ServerWorld? {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, key.toIdentifier()))
    }
}
