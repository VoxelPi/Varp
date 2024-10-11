package net.voxelpi.varp.cli.console

import net.voxelpi.varp.cli.command.VarpCLICommandManager
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class VarpCLIConsoleCompleter(
    private val console: VarpCLIConsole,
    private val commandManager: VarpCLICommandManager,
) : Completer {

    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
        val suggestions = commandManager.suggestionFactory().suggestImmediately(console, line.line())
        candidates += suggestions.list().map { Candidate(it.suggestion()) }
    }
}
