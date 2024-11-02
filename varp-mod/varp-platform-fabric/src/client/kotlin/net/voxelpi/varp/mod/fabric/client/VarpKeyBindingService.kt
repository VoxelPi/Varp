package net.voxelpi.varp.mod.fabric.client

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.voxelpi.varp.mod.fabric.FabricVarpMod
import net.voxelpi.varp.warp.path.RootPath
import org.lwjgl.glfw.GLFW

class VarpKeyBindingService(val client: FabricVarpClient) {

    val keyBindingOpenExplorer: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.${FabricVarpMod.MOD_ID}.open_explorer",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            VARP_KEY_BINDING_CATEGORY,
        )
    )

    init {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick)
    }

    private fun onTick(client: MinecraftClient) {
        while (keyBindingOpenExplorer.wasPressed()) {
            this.client.openExplorer(RootPath)
        }
    }

    companion object {
        const val VARP_KEY_BINDING_CATEGORY = "key.categories.varp"
    }
}
