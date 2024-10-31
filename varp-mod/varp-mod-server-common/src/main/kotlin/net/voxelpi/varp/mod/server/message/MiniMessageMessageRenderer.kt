package net.voxelpi.varp.mod.server.message

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.moonshine.message.IMessageRenderer
import java.lang.reflect.Method
import java.lang.reflect.Type

class MiniMessageMessageRenderer(
    tagResolvers: Iterable<TagResolver> = emptyList(),
) : IMessageRenderer<Audience, String, Component, Component> {

    val miniMessage = MiniMessage.builder().apply {
        editTags { it.resolvers(tagResolvers) }
    }.build()

    override fun render(
        receiver: Audience?,
        intermediateMessage: String,
        resolvedPlaceholders: Map<String, Component>,
        method: Method,
        owner: Type,
    ): Component {
        val placeholders = resolvedPlaceholders.map { (key, value) ->
            if (key.endsWith("_raw")) {
                Placeholder.parsed(key, PlainTextComponentSerializer.plainText().serialize(value))
            } else {
                Placeholder.component(key, value)
            }
        }

        return miniMessage.deserialize(intermediateMessage, *placeholders.toTypedArray())
    }
}
