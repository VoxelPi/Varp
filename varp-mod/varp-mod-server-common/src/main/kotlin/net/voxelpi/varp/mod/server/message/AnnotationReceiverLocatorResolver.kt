package net.voxelpi.varp.mod.server.message

import net.kyori.adventure.audience.Audience
import net.kyori.moonshine.receiver.IReceiverLocator
import net.kyori.moonshine.receiver.IReceiverLocatorResolver
import java.lang.reflect.Method
import java.lang.reflect.Type

object AnnotationReceiverLocatorResolver : IReceiverLocatorResolver<Audience> {

    override fun resolve(method: Method?, proxy: Type?): IReceiverLocator<Audience> {
        return AnnotationReceiverLocator
    }

    object AnnotationReceiverLocator : IReceiverLocator<Audience> {

        override fun locate(method: Method, proxy: Any, parameters: Array<out Any?>): Audience? {
            val receiver = method.parameters.withIndex().mapNotNull { (index, parameter) ->
                if (parameter != null && parameter.isAnnotationPresent(Receiver::class.java)) {
                    parameters[index]
                } else {
                    null
                }
            }.firstOrNull() ?: return null

            check(receiver is Audience) { "Receiver must be an Audience" }

            return receiver
        }
    }
}
