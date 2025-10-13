package net.voxelpi.varp.extras.cloud.parser.path

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeChildPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class NodeChildPathParser<C : Any>(
    public val treeSource: ((context: CommandContext<C>) -> Tree?)?,
) : ArgumentParser<C, NodeChildPath>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<NodeChildPath> {
        val input = commandInput.peekString()
        val path = NodeChildPath.parse(input).getOrElse {
            return ArgumentParseResult.failure(it)
        }

        commandInput.readString()
        return ArgumentParseResult.success(path)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource?.invoke(commandContext) ?: return emptyList()
        return tree.warps().map { it.path.toString() } + tree.folders().map { it.path.toString() }
    }
}

public fun <C : Any> nodeChildPathParser(treeSource: ((context: CommandContext<C>) -> Tree?)?): ParserDescriptor<C, NodeChildPath> {
    return ParserDescriptor.of(
        NodeChildPathParser(treeSource),
        NodeChildPath::class.java,
    )
}
