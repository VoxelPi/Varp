package net.voxelpi.varp.mod.fabric.client

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.util.Identifier
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpCreateFolderScreen
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpCreateWarpScreen
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpExplorerScreen
import net.voxelpi.varp.tree.path.RootPath
import org.lwjgl.glfw.GLFW

class VarpKeyBindingService(val client: FabricVarpClient) {

    val keyBindingCategory = KeyBinding.Category.create(Identifier.of(VARP_KEY_BINDING_CATEGORY))

    val keyBindingOpenExplorer: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.${FabricVarpMod.MOD_ID}.open_explorer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            keyBindingCategory,
        )
    )

    val keyBindingCreateWarp = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.${FabricVarpMod.MOD_ID}.create_warp",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            keyBindingCategory
        )
    )

    val keyBindingCreateFolder = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.${FabricVarpMod.MOD_ID}.create_folder",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            keyBindingCategory
        )
    )

    init {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick)
    }

    private fun onTick(client: MinecraftClient) {
        while (keyBindingOpenExplorer.wasPressed()) {
            this.client.openExplorer(RootPath)
        }
        while (keyBindingCreateWarp.wasPressed()) {
            val screen = client.currentScreen
            val parentPath = if (screen is FabricVarpExplorerScreen) screen.viewPath else RootPath
            client.setScreen(FabricVarpCreateWarpScreen(parentPath))
        }
        while (keyBindingCreateFolder.wasPressed()) {
            val screen = client.currentScreen
            val parentPath = if (screen is FabricVarpExplorerScreen) screen.viewPath else RootPath
            client.setScreen(FabricVarpCreateFolderScreen(parentPath))
        }
    }

    companion object {
        const val VARP_KEY_BINDING_CATEGORY = "key.categories.varp"
    }
}
