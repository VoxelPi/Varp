package net.voxelpi.varp.mod.server

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl

abstract class VarpServerImpl : VarpServerAPI {

    abstract val logger: ComponentLogger

    val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("mc-server"))

    abstract override val playerService: VarpServerPlayerServiceImpl<out VarpServerPlayerImpl>

    abstract val serverNetworkHandler: VarpServerNetworkHandler
}
