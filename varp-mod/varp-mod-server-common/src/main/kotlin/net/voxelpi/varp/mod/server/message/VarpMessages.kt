package net.voxelpi.varp.mod.server.message

import io.leangen.geantyref.TypeToken
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.Moonshine
import net.kyori.moonshine.annotation.Message
import net.kyori.moonshine.strategy.StandardPlaceholderResolverStrategy
import net.kyori.moonshine.strategy.supertype.StandardSupertypeThenInterfaceSupertypeStrategy
import net.voxelpi.varp.mod.server.message.placeholder.ComponentPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.NumberPlaceholderResolver
import net.voxelpi.varp.mod.server.message.placeholder.StringPlaceholderResolver
import org.spongepowered.configurate.ConfigurationNode

interface VarpMessages {

    @Message("prefix")
    fun prefix(): Component

    companion object {

        fun create(node: ConfigurationNode): VarpMessages {
            return Moonshine.builder<VarpMessages, Audience>(TypeToken.get(VarpMessages::class.java))
                .receiverLocatorResolver(AnnotationReceiverLocatorResolver, 0)
                .sourced(ConfigurateMessageSource(node))
                .rendered(
                    MiniMessageMessageRenderer(
                        listOf(
                            net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.parsed("prefix", node.node("prefix").getString("")),
                        )
                    )
                )
                .sent(AudienceMessageSender)
                .resolvingWithStrategy(StandardPlaceholderResolverStrategy(StandardSupertypeThenInterfaceSupertypeStrategy(true)))
                .apply {
                    weightedPlaceholderResolver(Component::class.java, ComponentPlaceholderResolver, 1)
                    weightedPlaceholderResolver(String::class.java, StringPlaceholderResolver, 1)
                    weightedPlaceholderResolver(Number::class.java, NumberPlaceholderResolver, 1)
                }
                .create(this::class.java.classLoader)
        }
    }
}
