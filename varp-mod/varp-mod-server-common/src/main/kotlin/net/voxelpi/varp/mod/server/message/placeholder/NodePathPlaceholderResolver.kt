package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.tree.path.NodePath
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.jvm.java
import kotlin.to

object NodePathPlaceholderResolver : IPlaceholderResolver<Audience, NodePath, Component> {

    override fun resolve(
        placeholderName: String,
        value: NodePath,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.right(ContinuanceValue.continuanceValue(value.value, String::class.java)),
            "${placeholderName}_id" to Either.right(ContinuanceValue.continuanceValue(value.key, String::class.java)),
        )
    }
}
