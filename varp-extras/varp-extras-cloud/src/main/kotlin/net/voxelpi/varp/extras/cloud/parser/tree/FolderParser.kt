package net.voxelpi.varp.extras.cloud.parser.tree

import net.voxelpi.varp.exception.tree.FolderNotFoundException
import net.voxelpi.varp.warp.Folder
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

public class FolderParser<C : Any>(
    public val treeSource: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, Folder>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C>,
        commandInput: CommandInput,
    ): ArgumentParseResult<Folder> {
        val input = commandInput.peekString()
        val path = FolderPath.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeSource(commandContext)
        val folder = tree.resolve(path)
            ?: return ArgumentParseResult.failure(FolderNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(folder)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeSource(commandContext)
        return tree.folders().map { it.path.toString() }
    }
}

public fun <C : Any> folderParser(treeSource: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, Folder> {
    return ParserDescriptor.of(
        FolderParser(treeSource),
        Folder::class.java,
    )
}
