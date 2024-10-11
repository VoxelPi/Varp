package net.voxelpi.varp.cli.console

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.kyori.adventure.text.logger.slf4j.ComponentLoggerProvider
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import net.kyori.adventure.translation.GlobalTranslator
import org.slf4j.LoggerFactory
import java.util.Locale

@Suppress("UnstableApiUsage")
class ComponentLoggerProviderImpl : ComponentLoggerProvider {

    override fun logger(helper: ComponentLoggerProvider.LoggerHelper, name: String): ComponentLogger {
        return helper.delegating(LoggerFactory.getLogger(name), ::serialize)
    }

    private fun serialize(message: Component): String {
        return ANSI_SERIALIZER.serialize(GlobalTranslator.render(message, Locale.getDefault()))
    }

    companion object {
        private val ANSI_SERIALIZER = ANSIComponentSerializer.ansi()
    }
}
