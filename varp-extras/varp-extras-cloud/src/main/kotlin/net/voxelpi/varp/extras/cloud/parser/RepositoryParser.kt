package net.voxelpi.varp.extras.cloud.parser

import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.exception.RepositoryNotFoundException
import net.voxelpi.varp.extras.cloud.VarpCommandArguments
import net.voxelpi.varp.repository.Repository
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider

public class RepositoryParser<C : Any>(
    public val environmentProvider: (context: CommandContext<C>) -> VarpEnvironment,
) : ArgumentParser<C, Repository>, BlockingSuggestionProvider.Strings<C> {

    override fun parse(commandContext: CommandContext<C>, commandInput: CommandInput): ArgumentParseResult<Repository> {
        val input = commandInput.peekString()
        val environment = environmentProvider.invoke(commandContext)

        val repo = environment.repositories[input]
            ?: return ArgumentParseResult.failure(RepositoryNotFoundException(input))

        commandInput.readString()
        return ArgumentParseResult.success(repo)
    }

    override fun stringSuggestions(commandContext: CommandContext<C>, input: CommandInput): Iterable<String> {
        val environment = environmentProvider.invoke(commandContext)
        return environment.repositories.keys
    }
}

public fun <C : Any> repositoryParser(environmentProvider: (context: CommandContext<C>) -> VarpEnvironment): ParserDescriptor<C, Repository> {
    return ParserDescriptor.of(
        RepositoryParser(environmentProvider),
        Repository::class.java,
    )
}

public fun <C : Any> repositoryParser(): ParserDescriptor<C, Repository> {
    return ParserDescriptor.of(
        RepositoryParser { it[VarpCommandArguments.ENVIRONMENT] },
        Repository::class.java,
    )
}
