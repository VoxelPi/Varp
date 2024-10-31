package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.to

object KeyPlaceholderResolver : IPlaceholderResolver<Audience, Key, Component> {

    override fun resolve(
        placeholderName: String,
        value: Key,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.left(ConclusionValue.conclusionValue(Component.text(value.asString())))
        )
    }
}
