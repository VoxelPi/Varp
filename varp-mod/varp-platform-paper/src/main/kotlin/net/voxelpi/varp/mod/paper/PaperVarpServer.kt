package net.voxelpi.varp.mod.paper

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.environment.VarpEnvironmentLoader
import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.paper.entity.PaperVarpServerEntityService
import net.voxelpi.varp.mod.paper.network.PaperVarpServerNetworkHandler
import net.voxelpi.varp.mod.paper.player.PaperVarpServerPlayerService
import net.voxelpi.varp.mod.server.VarpServerImpl
import net.voxelpi.varp.mod.server.api.VarpServer
import net.voxelpi.varp.mod.server.warp.VarpServerNetworkBridge
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryConfig
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryType
import net.voxelpi.varp.tree.path.RootPath
import org.bukkit.Server
import org.bukkit.plugin.ServicePriority
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div

class PaperVarpServer(
    val plugin: PaperVarpPlugin,
    val server: Server,
) : VarpServerImpl() {

    override val version: String
        get() = Varp.version

    override val info: VarpServerInformation = VarpServerInformation(version, VarpModConstants.PROTOCOL_VERSION, UUID.randomUUID())

    override val logger: ComponentLogger
        get() = plugin.componentLogger

    override val globalConfigDirectory: Path
        get() = plugin.dataPath

    override val eventScope: EventScope = eventScope()

    override val platform: PaperServerPlatform = PaperServerPlatform()

    init {
        plugin.commandService.registerServer(this)
        loadMessages()
    }

    override val loader: VarpEnvironmentLoader = VarpEnvironmentLoader.withStandardTypes(
        listOf(FileTreeRepositoryType)
    )

    private val environmentFilePath = plugin.dataPath / "data" / "server.varp.json"

    private val defaultEnvironment = EnvironmentDefinition.environmentDefinition {
        repository("default", FileTreeRepositoryType, FileTreeRepositoryConfig(environmentFilePath.parent / "repositories" / "default", "json", false)) {
            mountedAt(RootPath)
        }
    }

    override val environment: VarpEnvironment = VarpEnvironment.empty()

    override val serverNetworkHandler: PaperVarpServerNetworkHandler = PaperVarpServerNetworkHandler(this)

    override val serverNetworkBridge: VarpServerNetworkBridge = VarpServerNetworkBridge(tree, serverNetworkHandler)

    init {
        runBlocking {
            val definition = loader.load(environmentFilePath).getOrThrow() ?: defaultEnvironment
            environment.load(definition).getOrThrow()
        }
        plugin.componentLogger.info("Loaded ${environment.repositories.size} repositories")
        plugin.componentLogger.info("Loaded ${compositor.mounts().size} mounts")
        plugin.componentLogger.info("Loaded ${environment.tree.warps().size} warps")
    }

    override val playerService: PaperVarpServerPlayerService = PaperVarpServerPlayerService(this)

    override val entityService: PaperVarpServerEntityService = PaperVarpServerEntityService(this)

    init {
        // Register api service
        plugin.server.servicesManager.register(VarpServer::class.java, this, plugin, ServicePriority.Normal)
        VarpServer.register(this)
    }

    fun cleanup() {
        serverNetworkBridge.cleanup()
        runBlocking {
            loader.save(environment.save(), environmentFilePath)
            environment.deactivate()
        }
        coroutineScope.cancel()

        // Unregister api service.
        VarpServer.unregister()
    }

    override fun copyResourceTemplate(resource: String, destination: Path) {
        destination.parent.createDirectories()

        val template = plugin.getResource(resource)
        if (template == null) {
            logger.error("Resource \"$resource\" not found.")
            return
        }

        template.use { stream ->
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
