package net.voxelpi.varp.cli.command.parser

import net.kyori.adventure.key.Key
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import kotlin.jvm.java

class KeyParser<C : Any> : ArgumentParser<C, Key> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Key> {
        val input = commandInput.peekString()

        val key = try {
            Key.key(input)
        } catch (exception: Exception) {
            return ArgumentParseResult.failure(exception)
        }

        commandInput.readString()
        return ArgumentParseResult.success(key)
    }
}

fun <C : Any> keyParser(): ParserDescriptor<C, Key> {
    return ParserDescriptor.of(
        KeyParser<C>(),
        Key::class.java,
    )
}
