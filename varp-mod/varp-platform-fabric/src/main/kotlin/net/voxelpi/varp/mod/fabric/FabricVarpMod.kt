package net.voxelpi.varp.mod.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.fabric.server.FabricVarpServer
import net.voxelpi.varp.mod.fabric.server.command.FabricVarpCommandService

object FabricVarpMod : ModInitializer {

    val logger: ComponentLogger = ComponentLogger.logger("Varp")

    lateinit var commandService: FabricVarpCommandService

    private var varpServer: FabricVarpServer? = null

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
}
