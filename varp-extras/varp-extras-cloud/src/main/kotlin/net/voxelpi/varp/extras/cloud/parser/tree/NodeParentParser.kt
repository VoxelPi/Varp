package net.voxelpi.varp.extras.cloud.parser.tree

import net.voxelpi.varp.exception.tree.NodeParentNotFoundException
import net.voxelpi.varp.tree.NodeParent
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.NodeParentPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class NodeParentParser<C : Any>(
    public val treeSource: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, NodeParent>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodeParent> {
        val input = commandInput.peekString()
        val path = NodeParentPath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource(commandContext)
        val container = tree.resolve(path)
            ?: return ArgumentParseResult.failure(NodeParentNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(container)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val tree = treeSource(commandContext)
        return tree.containers().map { it.path.toString() }
    }
}

public fun <C : Any> nodeParentParser(treeSource: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, NodeParent> {
    return ParserDescriptor.of(
        NodeParentParser(treeSource),
        NodeParent::class.java,
    )
}
