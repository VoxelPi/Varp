package net.voxelpi.varp.extras.cloud.parser.tree

import net.voxelpi.varp.exception.tree.WarpNotFoundException
import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.WarpPath
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import kotlin.collections.map
import kotlin.getOrElse
import kotlin.jvm.java

public class WarpParser<C : Any>(
    public val treeProvider: (context: CommandContext<C>) -> Tree,
) : ArgumentParser<C, Warp>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(
        commandContext: CommandContext<C>,
        commandInput: CommandInput,
    ): ArgumentParseResult<Warp> {
        val input = commandInput.peekString()
        val path = WarpPath.Companion.parse(input).getOrElse { return ArgumentParseResult.failure(it) }

        val tree = treeProvider(commandContext)
        val warp = tree.resolve(path)
            ?: return ArgumentParseResult.failure(WarpNotFoundException(path))

        commandInput.readString()
        return ArgumentParseResult.success(warp)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): List<String> {
        val tree = treeProvider(commandContext)
        return tree.warps().map { it.path.toString() }
    }
}

public fun <C : Any> warpParser(treeProvider: (context: CommandContext<C>) -> Tree): ParserDescriptor<C, Warp> {
    return ParserDescriptor.of(
        WarpParser(treeProvider),
        Warp::class.java,
    )
}

public fun <C : Any> warpParser(): ParserDescriptor<C, Warp> {
    return ParserDescriptor.of(
        WarpParser { it[VarpCommandArguments.TREE] },
        Warp::class.java,
    )
}
