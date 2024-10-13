package net.voxelpi.varp.cli.command.parser.path

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.WarpPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import kotlin.collections.map
import kotlin.getOrElse
import kotlin.jvm.java

class WarpPathParser<C : Any>(
    val treeSource: (() -> Tree?)?,
) : ArgumentParser<C, WarpPath>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C>,
        commandInput: CommandInput,
    ): ArgumentParseResult<WarpPath> {
        val input = commandInput.peekString()
        val path = WarpPath.parse(input).getOrElse {
            return ArgumentParseResult.failure(it)
        }

        commandInput.readString()
        return ArgumentParseResult.success(path)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource?.invoke() ?: return emptyList()
        return tree.warps().map { it.path.toString() }
    }
}

fun <C : Any> warpPathParser(treeSource: (() -> Tree?)?): ParserDescriptor<C, WarpPath> {
    return ParserDescriptor.of(
        WarpPathParser<C>(treeSource),
        WarpPath::class.java,
    )
}
