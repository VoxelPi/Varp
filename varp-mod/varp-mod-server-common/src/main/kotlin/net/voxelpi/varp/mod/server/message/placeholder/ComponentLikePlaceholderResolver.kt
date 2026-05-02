package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import java.lang.reflect.Method
import java.lang.reflect.Type

object ComponentLikePlaceholderResolver : IPlaceholderResolver<Audience, ComponentLike, Component> {

    override fun resolve(
        placeholderName: String,
        value: ComponentLike?,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): Map<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mapOf(
            placeholderName to Either.left(ConclusionValue.conclusionValue(value?.asComponent() ?: Component.text("null"))),
        )
    }
}
