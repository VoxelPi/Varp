package net.voxelpi.varp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage

@JvmRecord
public data class ComponentTemplate(
    val originalMessage: String,
) : ComponentLike {

    override fun asComponent(): Component {
        return miniMessage().deserialize(originalMessage)
    }
}
