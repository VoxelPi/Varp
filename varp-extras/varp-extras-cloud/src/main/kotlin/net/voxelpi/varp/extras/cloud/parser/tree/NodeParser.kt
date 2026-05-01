package net.voxelpi.varp.extras.cloud.parser.tree

import net.voxelpi.varp.exception.tree.NodeNotFoundException
import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.tree.Node
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.NodePath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class NodeParser<C : Any>(
    public val treeProvider: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, Node>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Node> {
        val input = commandInput.peekString()
        val path = NodePath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeProvider(commandContext)
        val container = tree.resolve(path)
            ?: return ArgumentParseResult.failure(NodeNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(container)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val tree = treeProvider(commandContext)
        return tree.warps().map { it.path.toString() } + tree.containers().map { it.path.toString() }
    }
}

public fun <C : Any> nodeParser(treeProvider: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, Node> {
    return ParserDescriptor.of(
        NodeParser(treeProvider),
        Node::class.java,
    )
}

public fun <C : Any> nodeParser(): ParserDescriptor<C, Node> {
    return ParserDescriptor.of(
        NodeParser { it[VarpCommandArguments.TREE] },
        Node::class.java,
    )
}
