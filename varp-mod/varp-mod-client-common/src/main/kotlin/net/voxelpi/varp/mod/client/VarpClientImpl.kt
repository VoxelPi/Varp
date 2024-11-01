package net.voxelpi.varp.mod.client

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.mod.api.VarpServerInformation
import net.voxelpi.varp.mod.client.api.VarpClientAPI
import net.voxelpi.varp.mod.client.network.VarpClientNetworkHandler
import net.voxelpi.varp.mod.client.warp.ClientRepositoryImpl
import net.voxelpi.varp.mod.network.protocol.serverbound.VarpServerboundClientInfoPacket

abstract class VarpClientImpl() : VarpClientAPI {

    abstract val logger: ComponentLogger

    val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("mc-client"))

    abstract val clientNetworkHandler: VarpClientNetworkHandler

    abstract override val repository: ClientRepositoryImpl

    override var serverInfo: VarpServerInformation? = null
        protected set

    fun requestBridgeInitialization() {
        clientNetworkHandler.sendServerboundPacket(VarpServerboundClientInfoPacket(info))
    }

    fun enableBridge(serverInfo: VarpServerInformation) {
        // Skip if the server has not changed.
        if (serverInfo == this.serverInfo) {
            return
        }

        this.serverInfo = serverInfo
        logger.info("Received server info: version: ${serverInfo.version}, protocol version: ${serverInfo.protocolVersion}, identifier: ${serverInfo.identifier}.")
        logger.info("Activated varp client-server bridge")
    }

    fun disableBridge() {
        serverInfo = null
        repository.reset()
        logger.info("Deactivated varp client-server bridge")
    }
}
