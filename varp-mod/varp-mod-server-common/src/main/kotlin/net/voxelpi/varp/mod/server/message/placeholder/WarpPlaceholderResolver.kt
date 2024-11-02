package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.WarpPath
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.jvm.java
import kotlin.to

object WarpPlaceholderResolver : IPlaceholderResolver<Audience, Warp, Component> {

    override fun resolve(
        placeholderName: String,
        value: Warp,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.left(ConclusionValue.conclusionValue(value.name)),
            "${placeholderName}_path" to Either.right(ContinuanceValue.continuanceValue(value.path, WarpPath::class.java)),
            "${placeholderName}_id" to Either.right(ContinuanceValue.continuanceValue(value.id, String::class.java)),
            "${placeholderName}_name" to Either.left(ConclusionValue.conclusionValue(value.name)),
            "${placeholderName}_description" to Either.left(ConclusionValue.conclusionValue(Component.join(JoinConfiguration.newlines(), value.description))),
            "${placeholderName}_tags" to Either.left(ConclusionValue.conclusionValue(Component.text(value.tags.joinToString(", ", "[", "]")))),
            "${placeholderName}_properties" to Either.left(ConclusionValue.conclusionValue(Component.text(value.properties.map { "${it.key}: ${it.value}" }.joinToString(", ", "{", "}")))),
            "${placeholderName}_location" to Either.right(ContinuanceValue.continuanceValue(value.location, MinecraftLocation::class.java)),
        )
    }
}