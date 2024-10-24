package net.voxelpi.varp.mod.client

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.client.api.VarpClientAPI
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
import net.voxelpi.varp.mod.client.warp.ClientRepositoryImpl

abstract class VarpClientImpl() : VarpClientAPI {

    abstract val logger: ComponentLogger

    val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("mc-client"))

    abstract val clientNetworkHandler: VarpClientNetworkHandler

    abstract override val repository: ClientRepositoryImpl
}
