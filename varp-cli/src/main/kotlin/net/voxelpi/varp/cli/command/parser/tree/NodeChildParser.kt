package net.voxelpi.varp.cli.command.parser.tree

import net.voxelpi.varp.exception.tree.NodeChildNotFoundException
import net.voxelpi.varp.warp.NodeChild
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeChildPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

class NodeChildParser<C : Any>(
    val treeSource: () -> Tree,
) : ArgumentParser<C, NodeChild>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodeChild> {
        val input = commandInput.peekString()
        val path = NodeChildPath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource()
        val container = tree.resolve(path)
            ?: return ArgumentParseResult.failure(NodeChildNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(container)
    }

    override fun stringSuggestions(commandContext: CommandContext<C?>, input: CommandInput): Iterable<String> {
        val tree = treeSource()
        return tree.warps().map { it.path.toString() } + tree.folders().map { it.path.toString() }
    }
}

fun <C : Any> nodeChildParser(treeSource: () -> Tree): ParserDescriptor<C, NodeChild> {
    return ParserDescriptor.of(
        NodeChildParser<C>(treeSource),
        NodeChild::class.java,
    )
}
