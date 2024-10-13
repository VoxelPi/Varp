package net.voxelpi.varp.cli.command.parser.path

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class NodeParentPathParser<C : Any>(
    val treeSource: (() -> Tree?)?,
) : ArgumentParser<C, NodeParentPath>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodeParentPath> {
        val input = commandInput.peekString()
        val path = NodeParentPath.parse(input).getOrElse {
            return ArgumentParseResult.failure(it)
        }

        commandInput.readString()
        return ArgumentParseResult.success(path)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource?.invoke() ?: return emptyList()
        return tree.containers().map { it.path.toString() }
    }
}

fun <C : Any> nodeParentPathParser(treeSource: (() -> Tree?)?): ParserDescriptor<C, NodeParentPath> {
    return ParserDescriptor.of(
        NodeParentPathParser<C>(treeSource),
        NodeParentPath::class.java,
    )
}
