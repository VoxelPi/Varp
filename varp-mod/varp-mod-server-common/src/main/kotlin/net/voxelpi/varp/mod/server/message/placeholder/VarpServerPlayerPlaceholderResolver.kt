package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.mod.server.api.player.VarpServerPlayer
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.jvm.java
import kotlin.to

object VarpServerPlayerPlaceholderResolver : IPlaceholderResolver<Audience, VarpServerPlayer, Component> {

    override fun resolve(
        placeholderName: String,
        value: VarpServerPlayer,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.left(ConclusionValue.conclusionValue(value.displayName)),
            "${placeholderName}_name" to Either.right(ContinuanceValue.continuanceValue(value.username, String::class.java)),
            "${placeholderName}_uuid" to Either.right(ContinuanceValue.continuanceValue(value.uniqueId.toString(), String::class.java)),
        )
    }
}
