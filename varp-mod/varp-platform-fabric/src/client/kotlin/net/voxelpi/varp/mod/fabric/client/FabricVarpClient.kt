package net.voxelpi.varp.mod.fabric.client

import kotlinx.coroutines.cancel
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.Varp
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.warp.ClientRepositoryImpl
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.client.network.FabricVarpClientNetworkHandler
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath

class FabricVarpClient : VarpClientImpl() {

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val version: String
        get() = Varp.version

    override val info: VarpClientInformation = VarpClientInformation(version, VarpModConstants.PROTOCOL_VERSION)

    override val clientNetworkHandler: FabricVarpClientNetworkHandler = FabricVarpClientNetworkHandler(this)

    override val repository: ClientRepositoryImpl = ClientRepositoryImpl(this, clientNetworkHandler, "main")

    val keyBindingService = VarpKeyBindingService(this)

    override val tree: Tree
        get() = repository.tree

    override fun openExplorer(path: NodeParentPath) {
        TODO("Not yet implemented")
    }

    fun cleanup() {
        coroutineScope.cancel()
    }
}
