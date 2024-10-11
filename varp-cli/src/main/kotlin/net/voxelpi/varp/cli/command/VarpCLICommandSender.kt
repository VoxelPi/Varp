package net.voxelpi.varp.cli.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

interface VarpCLICommandSender {

    fun sendMessage(message: String) {
        sendMessage(MiniMessage.miniMessage().deserialize(message))
    }

    fun sendMessage(message: Component)
}
