package net.voxelpi.varp.cli.command.parser.tree

import net.voxelpi.varp.exception.tree.NodeNotFoundException
import net.voxelpi.varp.warp.Node
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodePath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class NodeParser<C : Any>(
    val treeSource: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, Node>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Node> {
        val input = commandInput.peekString()
        val path = NodePath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource(commandContext)
        val container = tree.resolve(path)
            ?: return ArgumentParseResult.failure(NodeNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(container)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val tree = treeSource(commandContext)
        return tree.warps().map { it.path.toString() } + tree.containers().map { it.path.toString() }
    }
}

fun <C : Any> nodeParser(treeSource: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, Node> {
    return ParserDescriptor.of(
        NodeParser<C>(treeSource),
        Node::class.java,
    )
}
