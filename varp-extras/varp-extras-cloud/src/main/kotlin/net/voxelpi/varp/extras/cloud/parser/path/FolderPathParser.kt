package net.voxelpi.varp.extras.cloud.parser.path

import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.FolderPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import kotlin.collections.map
import kotlin.getOrElse
import kotlin.jvm.java

public class FolderPathParser<C : Any>(
    public val treeSource: ((context: CommandContext<C>) -> Tree?)?,
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
        val tree = treeSource?.invoke(commandContext) ?: return emptyList()
        return tree.containers().map { it.path.toString() }
    }
}

public fun <C : Any> folderPathParser(treeSource: ((context: CommandContext<C>) -> Tree?)?): ParserDescriptor<C, FolderPath> {
    return ParserDescriptor.of(
        FolderPathParser(treeSource),
        FolderPath::class.java,
    )
}
