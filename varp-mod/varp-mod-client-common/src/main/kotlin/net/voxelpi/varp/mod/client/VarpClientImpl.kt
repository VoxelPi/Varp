package net.voxelpi.varp.mod.client

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.client.api.VarpClientAPI
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
import net.voxelpi.varp.mod.client.warp.ClientRepositoryImpl

interface VarpClientImpl : VarpClientAPI {

    val logger: ComponentLogger

    val clientNetworkHandler: VarpClientNetworkHandler

    override val repository: ClientRepositoryImpl
}
