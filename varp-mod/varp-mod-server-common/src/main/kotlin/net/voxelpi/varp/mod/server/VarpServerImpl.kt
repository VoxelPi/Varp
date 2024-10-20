package net.voxelpi.varp.mod.server

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.server.api.VarpServerAPI
import net.voxelpi.varp.mod.server.network.VarpServerNetworkHandler
import net.voxelpi.varp.mod.server.player.VarpServerPlayerImpl
import net.voxelpi.varp.mod.server.player.VarpServerPlayerServiceImpl

interface VarpServerImpl : VarpServerAPI {

    val logger: ComponentLogger

    override val playerService: VarpServerPlayerServiceImpl<out VarpServerPlayerImpl>

    val serverNetworkHandler: VarpServerNetworkHandler
}
