package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import java.lang.reflect.Method
import java.lang.reflect.Type

object StringPlaceholderResolver : IPlaceholderResolver<Audience, String, Component> {

    override fun resolve(
        placeholderName: String,
        value: String?,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): Map<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mapOf(
            placeholderName to Either.left(ConclusionValue.conclusionValue(Component.text(value ?: "null"))),
            "${placeholderName}_raw" to Either.left(ConclusionValue.conclusionValue(Component.text(value ?: "null"))),
        )
    }
}
