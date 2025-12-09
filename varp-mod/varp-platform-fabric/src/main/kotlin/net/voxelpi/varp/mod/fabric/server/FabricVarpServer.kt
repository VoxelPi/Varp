package net.voxelpi.varp.mod.fabric.server

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.WorldSavePath
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.environment.VarpEnvironmentLoader
import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.server.entity.FabricVarpServerEntityService
import net.voxelpi.varp.mod.fabric.server.network.FabricVarpServerNetworkHandler
import net.voxelpi.varp.mod.fabric.server.player.FabricVarpServerPlayerService
import net.voxelpi.varp.mod.fabric.util.toIdentifier
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServer
import net.voxelpi.varp.mod.server.warp.VarpServerNetworkBridge
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryConfig
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryType
import net.voxelpi.varp.tree.path.RootPath
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.div

class FabricVarpServer(
    val server: MinecraftServer,
) : VarpServerImpl() {

    override val version: String
        get() = Varp.version

    override val info: VarpServerInformation = VarpServerInformation(version, VarpModConstants.PROTOCOL_VERSION, UUID.randomUUID())

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val globalConfigDirectory: Path
        get() = FabricVarpMod.configDirectory

    override val eventScope: EventScope = eventScope()

    override val platform: FabricServerPlatform = FabricServerPlatform()

    val serverAudiences: MinecraftServerAudiences = MinecraftServerAudiences.of(server)

    init {
        loadMessages()
    }

    override val loader: VarpEnvironmentLoader = VarpEnvironmentLoader.withStandardTypes(
        listOf(FileTreeRepositoryType)
    )

    private val environmentFilePath = server.getSavePath(WorldSavePath.ROOT) / "data" / "varp" / "server.varp.json"

    private val defaultEnvironment = EnvironmentDefinition.environmentDefinition {
        repository("default", FileTreeRepositoryType, FileTreeRepositoryConfig(environmentFilePath.parent / "repositories" / "default", "json", false)) {
            mountedAt(RootPath)
        }
    }

    override val environment: VarpEnvironment = VarpEnvironment.empty()

    override val serverNetworkHandler: FabricVarpServerNetworkHandler = FabricVarpServerNetworkHandler(this)

    override val serverNetworkBridge: VarpServerNetworkBridge = VarpServerNetworkBridge(tree, serverNetworkHandler)

    init {
        runBlocking {
            val definition = loader.load(environmentFilePath).getOrThrow() ?: defaultEnvironment
            environment.load(definition).getOrThrow()
        }
        FabricVarpMod.logger.info("Loaded ${environment.repositories.size} repositories")
        FabricVarpMod.logger.info("Loaded ${compositor.mounts().size} mounts")
        FabricVarpMod.logger.info("Loaded ${environment.tree.warps().size} warps")
    }

    override val playerService: FabricVarpServerPlayerService = FabricVarpServerPlayerService(this)

    override val entityService: FabricVarpServerEntityService = FabricVarpServerEntityService(this)

    init {
        // Register api service
        VarpServer.register(this)
    }

    fun cleanup() {
        serverNetworkBridge.cleanup()
        serverNetworkHandler.cleanup()
        playerService.handleShutdown()
        runBlocking {
            loader.save(environment.save(), environmentFilePath)
            environment.deactivate()
        }
        coroutineScope.cancel()

        // Unregister api service.
        VarpServer.unregister()
    }

    override fun copyResourceTemplate(resource: String, destination: Path) {
        FabricVarpMod.copyResourceTemplate(resource, destination)
    }

    fun world(key: Key): ServerWorld? {
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, key.toIdentifier()))
    }
}
