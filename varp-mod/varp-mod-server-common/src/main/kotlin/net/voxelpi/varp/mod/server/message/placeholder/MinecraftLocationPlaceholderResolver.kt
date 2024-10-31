package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.MinecraftLocation
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.jvm.java
import kotlin.to

object MinecraftLocationPlaceholderResolver : IPlaceholderResolver<Audience, MinecraftLocation, Component> {

    override fun resolve(
        placeholderName: String,
        value: MinecraftLocation,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.right(ContinuanceValue.continuanceValue("(${value.world}, ${value.x}, ${value.y}, ${value.z})", String::class.java)),
            "${placeholderName}_world" to Either.right(ContinuanceValue.continuanceValue(value.world.asString(), String::class.java)),
            "${placeholderName}_x" to Either.right(ContinuanceValue.continuanceValue(value.x, Number::class.java)),
            "${placeholderName}_y" to Either.right(ContinuanceValue.continuanceValue(value.y, Number::class.java)),
            "${placeholderName}_z" to Either.right(ContinuanceValue.continuanceValue(value.z, Number::class.java)),
            "${placeholderName}_yaw" to Either.right(ContinuanceValue.continuanceValue(value.yaw, Number::class.java)),
            "${placeholderName}_pitch" to Either.right(ContinuanceValue.continuanceValue(value.pitch, Number::class.java)),
        )
    }
}
