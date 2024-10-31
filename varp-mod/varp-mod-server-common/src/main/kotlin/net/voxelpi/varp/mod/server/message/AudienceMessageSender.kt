package net.voxelpi.varp.mod.server.message

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.message.IMessageSender

object AudienceMessageSender : IMessageSender<Audience, Component> {

    override fun send(receiver: Audience?, renderedMessage: Component) {
        receiver?.sendMessage(renderedMessage)
    }
}
