package net.voxelpi.varp.mod.fabric.client.util

import net.kyori.adventure.platform.fabric.FabricClientAudiences
import net.kyori.adventure.text.Component
import net.minecraft.text.Text

fun Component.clientNative(): Text {
    return FabricClientAudiences.of().toNative(this)
}
