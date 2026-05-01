package net.voxelpi.varp.extras.cloud.parser.path

import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.FolderPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class FolderPathParser<C : Any>(
    public val treeProvider: ((context: CommandContext<C>) -> Tree?)?,
) : ArgumentParser<C, FolderPath>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C>,
        commandInput: CommandInput,
    ): ArgumentParseResult<FolderPath> {
        val input = commandInput.peekString()
        val path = FolderPath.parse(input).getOrElse {
            return ArgumentParseResult.failure(it)
        }

        commandInput.readString()
        return ArgumentParseResult.success(path)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeProvider?.invoke(commandContext) ?: return emptyList()
        return tree.containers().map { it.path.toString() }
    }
}

public fun <C : Any> folderPathParser(treeProvider: (context: CommandContext<C>) -> Tree?): ParserDescriptor<C, FolderPath> {
    return ParserDescriptor.of(
        FolderPathParser(treeProvider),
        FolderPath::class.java,
    )
}

public fun <C : Any> folderPathParser(): ParserDescriptor<C, FolderPath> {
    return ParserDescriptor.of(
        FolderPathParser { it[VarpCommandArguments.TREE] },
        FolderPath::class.java,
    )
}
