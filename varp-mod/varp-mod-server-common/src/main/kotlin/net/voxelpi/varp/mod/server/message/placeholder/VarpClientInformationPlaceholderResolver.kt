package net.voxelpi.varp.mod.server.message.placeholder

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.moonshine.placeholder.ConclusionValue
import net.kyori.moonshine.placeholder.ContinuanceValue
import net.kyori.moonshine.placeholder.IPlaceholderResolver
import net.kyori.moonshine.util.Either
import net.voxelpi.varp.mod.api.VarpClientInformation
import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.jvm.java
import kotlin.to

object VarpClientInformationPlaceholderResolver : IPlaceholderResolver<Audience, VarpClientInformation, Component> {

    override fun resolve(
        placeholderName: String,
        value: VarpClientInformation,
        receiver: Audience?,
        owner: Type,
        method: Method,
        parameters: Array<out Any?>,
    ): MutableMap<String, Either<ConclusionValue<out Component>, ContinuanceValue<*>>> {
        return mutableMapOf(
            "${placeholderName}_version" to Either.right(ContinuanceValue.continuanceValue(value.version, String::class.java)),
            "${placeholderName}_protocol_version" to Either.right(ContinuanceValue.continuanceValue(value.protocolVersion, Number::class.java)),
        )
    }
}
