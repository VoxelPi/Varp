package net.voxelpi.varp.mod.fabric.client

import kotlinx.coroutines.cancel
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.client.Minecraft
import net.voxelpi.varp.Varp
import net.voxelpi.varp.mod.VarpModConstants
import net.voxelpi.varp.mod.api.VarpClientInformation
import net.voxelpi.varp.mod.client.VarpClientImpl
import net.voxelpi.varp.mod.client.tree.VarpClientTree
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.client.gui.FabricVarpGUIListener
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpExplorerScreen
import net.voxelpi.varp.mod.fabric.client.network.FabricVarpClientNetworkHandler
import net.voxelpi.varp.tree.path.NodeParentPath

class FabricVarpClient : VarpClientImpl() {

    override val logger: ComponentLogger
        get() = FabricVarpMod.logger

    override val version: String
        get() = Varp.version

    override val info: VarpClientInformation = VarpClientInformation(version, VarpModConstants.PROTOCOL_VERSION)

    override val clientNetworkHandler: FabricVarpClientNetworkHandler = FabricVarpClientNetworkHandler(this)

    override val tree: VarpClientTree = VarpClientTree(this, clientNetworkHandler)

    private val guiRepositoryListener = FabricVarpGUIListener(tree)

    val keyBindingService = VarpKeyBindingService(this)

    val audience = MinecraftClientAudiences.of().audience()

    override fun openExplorer(path: NodeParentPath) {
        if (!isBridgeEnabled()) {
            audience.sendMessage(Component.translatable("varp.bridge_not_active"))
        }
        Minecraft.getInstance().gui.setScreen(FabricVarpExplorerScreen(path))
    }

    fun cleanup() {
        guiRepositoryListener.cleanup()
        coroutineScope.cancel()
    }
}
