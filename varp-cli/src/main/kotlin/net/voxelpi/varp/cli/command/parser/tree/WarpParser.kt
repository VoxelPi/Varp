package net.voxelpi.varp.cli.command.parser.tree

import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.Warp
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

class WarpParser<C : Any>(
    val treeSource: () -> Tree,
) : ArgumentParser<C, Warp>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C>,
        commandInput: CommandInput,
    ): ArgumentParseResult<Warp> {
        val input = commandInput.peekString()
        val path = WarpPath.Companion.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource()
        val warp = tree.warp(path)
            ?: return ArgumentParseResult.failure(WarpNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(warp)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource()
        return tree.warps().map { it.path.toString() }
    }
}

fun <C : Any> warpParser(treeSource: () -> Tree): ParserDescriptor<C, Warp> {
    return ParserDescriptor.of(
        WarpParser<C>(treeSource),
        Warp::class.java,
    )
}