package net.voxelpi.varp.mod.fabric.client.util

import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.Component
import net.minecraft.text.Text

fun Component.clientNative(): Text {
    return MinecraftClientAudiences.of().nonWrappingSerializer().serialize(this)
}
