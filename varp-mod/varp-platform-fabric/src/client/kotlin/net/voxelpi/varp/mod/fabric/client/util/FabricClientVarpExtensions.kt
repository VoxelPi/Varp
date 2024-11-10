package net.voxelpi.varp.mod.fabric.client.util

import net.kyori.adventure.platform.modcommon.MinecraftAudiences
import net.kyori.adventure.text.Component
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

fun Component.clientNative(): Text {
    return MinecraftAudiences.nonWrappingSerializer { MinecraftClient.getInstance().server?.registryManager }.serialize(this)
}
