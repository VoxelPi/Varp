package net.voxelpi.varp.mod.server.message

import net.kyori.adventure.audience.Audience
import net.kyori.moonshine.message.IMessageSource
import org.spongepowered.configurate.ConfigurationNode

class ConfigurateMessageSource(
    private val node: ConfigurationNode,
) : IMessageSource<Audience, String> {

    override fun messageOf(receiver: Audience?, messageKey: String): String? {
        val node = node.node(messageKey.split("."))

        return when {
            node.virtual() -> "unknown message (${node.path().joinToString(".")})"
            node.isList -> node.childrenList().joinToString("\n<reset>") { it.getString("unknown message list entry (${node.path().joinToString(".")})") }
            else -> node.getString("unknown message entry (${node.path().joinToString(".")})")
        }
    }
}
