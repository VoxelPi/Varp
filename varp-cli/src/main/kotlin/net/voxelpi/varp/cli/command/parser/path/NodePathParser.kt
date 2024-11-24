package net.voxelpi.varp.cli.command.parser.path

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodePath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class NodePathParser<C : Any>(
    val treeSource: ((context: CommandContext<C>) -> Tree?)?,
) : ArgumentParser<C, NodePath>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodePath> {
        val input = commandInput.peekString()
        val path = NodePath.parse(input).getOrElse {
            return ArgumentParseResult.failure(it)
        }

        commandInput.readString()
        return ArgumentParseResult.success(path)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource?.invoke(commandContext) ?: return emptyList()
        return tree.warps().map { it.path.toString() } + tree.containers().map { it.path.toString() }
    }
}

fun <C : Any> nodePathParser(treeSource: ((context: CommandContext<C>) -> Tree?)?): ParserDescriptor<C, NodePath> {
    return ParserDescriptor.of(
        NodePathParser<C>(treeSource),
        NodePath::class.java,
    )
}
