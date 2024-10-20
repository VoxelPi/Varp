package net.voxelpi.varp.mod.fabric.client

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.voxelpi.varp.Varp
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.api.warp.ClientRepository
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.client.network.FabricVarpClientNetworkHandler
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath

class FabricVarpClient : VarpClientImpl {

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val version: String
        get() = Varp.version

    override val repository: ClientRepository
        get() = TODO("Not yet implemented")

    override val tree: Tree
        get() = TODO("Not yet implemented")

    override val clientNetworkHandler: FabricVarpClientNetworkHandler = FabricVarpClientNetworkHandler(this)

    override fun openExplorer(path: NodeParentPath) {
        TODO("Not yet implemented")
    }
}
