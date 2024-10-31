package net.voxelpi.varp.mod.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandService
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.jvm.optionals.getOrElse

object FabricVarpMod : ModInitializer {

    const val MOD_ID = "varp"

    val modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()

    val logger: ComponentLogger = ComponentLogger.logger("Varp")

    lateinit var commandService: FabricVarpCommandService

    private var varpServer: FabricVarpServer? = null

    val configDirectory = FabricLoader.getInstance().configDir.resolve("varp")

    override fun onInitialize() {
        commandService = FabricVarpCommandService()

        // Server lifecycle event handlers.
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            varpServer = FabricVarpServer(server)
        }
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            varpServer?.cleanup()
            varpServer = null
        }

        // Player events.
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            varpServer?.playerService?.handleJoin(handler)
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            varpServer?.playerService?.handleQuit(handler)
        }
    }

    fun copyResourceTemplate(resource: String, destination: Path) {
        destination.parent.createDirectories()

        val template = modContainer.findPath(resource).getOrElse {
            logger.error("Resource \"$resource\" not found.")
            return
        }

        template.inputStream().use { stream ->
            Files.copy(stream, destination, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
