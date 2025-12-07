package net.voxelpi.varp.extras.cloud.parser.tree

import net.voxelpi.varp.exception.tree.NodeChildNotFoundException
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.NodeChildPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class NodeChildParser<C : Any>(
    public val treeSource: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, NodeChild>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodeChild> {
        val input = commandInput.peekString()
        val path = NodeChildPath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource(commandContext)
        val container = tree.resolve(path)
            ?: return ArgumentParseResult.failure(NodeChildNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(container)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val tree = treeSource(commandContext)
        return tree.warps().map { it.path.toString() } + tree.folders().map { it.path.toString() }
    }
}

public fun <C : Any> nodeChildParser(treeSource: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, NodeChild> {
    return ParserDescriptor.of(
        NodeChildParser(treeSource),
        NodeChild::class.java,
    )
}
