package net.voxelpi.varp.mod.server

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.mod.server.message.VarpMessages
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl
import net.voxelpi.varp.mod.server.warp.VarpServerNetworkBridge
import org.spongepowered.configurate.gson.GsonConfigurationLoader
import java.nio.file.Path
import kotlin.io.path.exists

abstract class VarpServerImpl : VarpServerAPI {

    abstract val logger: ComponentLogger

    val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("mc-server"))

    abstract override val platform: ServerPlatformImpl

    protected abstract val globalConfigDirectory: Path

    lateinit var messages: VarpMessages
        private set

    abstract override val playerService: VarpServerPlayerServiceImpl<out VarpServerPlayerImpl>

    abstract val serverNetworkHandler: VarpServerNetworkHandler

    abstract val serverNetworkBridge: VarpServerNetworkBridge

    fun loadMessages(): VarpMessages {
        val localeFile = globalConfigDirectory.resolve("locale").resolve("en_us.json")

        if (!localeFile.exists()) {
            copyResourceTemplate("locale/en_us.json", localeFile)
        }

        val node = GsonConfigurationLoader.builder().apply {
            path(localeFile)
        }.build().load()

        if (node == null) {
            logger.error("Unable to load server messages.")
            throw IllegalStateException("Unable to load server messages.")
        }

        val messages = VarpMessages.create(node)
        this.messages = messages
        return messages
    }

    protected abstract fun copyResourceTemplate(resource: String, destination: Path)
}
