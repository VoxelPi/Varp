package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.ContinuanceValue.continuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.repository.Repository
import java.lang.reflect.Method
import java.lang.reflect.Type

object RepositoryPlaceholderResolver : IPlaceholderResolver<Audience, Repository, Component> {

    override fun resolve(
        placeholderName: String,
        value: Repository,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            placeholderName to Either.right(continuanceValue(value.id, String::class.java)),
            "${placeholderName}_id" to Either.right(continuanceValue(value.id, String::class.java)),
            "${placeholderName}_type" to Either.right(continuanceValue(value.type.id, String::class.java)),
        )
    }
}
