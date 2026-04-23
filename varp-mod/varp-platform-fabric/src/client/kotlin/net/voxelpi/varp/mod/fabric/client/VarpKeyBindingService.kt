package net.voxelpi.varp.mod.fabric.client

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpCreateFolderScreen
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpCreateWarpScreen
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpExplorerScreen
import net.voxelpi.varp.tree.path.RootPath
import org.lwjgl.glfw.GLFW

class VarpKeyBindingService(val client: FabricVarpClient) {

    val keyBindingCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(FabricVarpMod.MOD_ID, VARP_KEY_BINDING_CATEGORY))

    val keyBindingOpenExplorer: KeyMapping = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.${FabricVarpMod.MOD_ID}.open_explorer",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            keyBindingCategory,
        )
    )

    val keyBindingCreateWarp = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.${FabricVarpMod.MOD_ID}.create_warp",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            keyBindingCategory
        )
    )

    val keyBindingCreateFolder = KeyMappingHelper.registerKeyMapping(
        KeyMapping(
            "key.${FabricVarpMod.MOD_ID}.create_folder",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            keyBindingCategory
        )
    )

    init {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick)
    }

    private fun onTick(client: Minecraft) {
        while (keyBindingOpenExplorer.consumeClick()) {
            this.client.openExplorer(RootPath)
        }
        while (keyBindingCreateWarp.consumeClick()) {
            val screen = client.screen
            val parentPath = if (screen is FabricVarpExplorerScreen) screen.viewPath else RootPath
            client.setScreen(FabricVarpCreateWarpScreen(parentPath))
        }
        while (keyBindingCreateFolder.consumeClick()) {
            val screen = client.screen
            val parentPath = if (screen is FabricVarpExplorerScreen) screen.viewPath else RootPath
            client.setScreen(FabricVarpCreateFolderScreen(parentPath))
        }
    }

    companion object {
        const val VARP_KEY_BINDING_CATEGORY = "key.categories.varp"
    }
}
