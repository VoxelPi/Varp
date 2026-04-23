package net.voxelpi.varp.mod.fabric.client.util

import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences
import net.kyori.adventure.text.Component

fun Component.clientNative(): net.minecraft.network.chat.Component {
    return MinecraftClientAudiences.of().nonWrappingSerializer().serialize(this)
}
