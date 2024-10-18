package net.voxelpi.varp.cli.console

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.minecrell.terminalconsole.TerminalConsoleAppender
import net.voxelpi.varp.VarpBuildParameters
import net.voxelpi.varp.cli.VarpCLI
import net.voxelpi.varp.cli.command.VarpCLICommandManager
import net.voxelpi.varp.cli.command.VarpCLICommandSender
import net.voxelpi.varp.exception.tree.FolderMoveIntoChildException
import org.incendo.cloud.exception.ArgumentParseException
import org.incendo.cloud.exception.CommandExecutionException
import org.incendo.cloud.exception.InvalidSyntaxException
import org.incendo.cloud.exception.NoSuchCommandException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.utils.InfoCmp
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

class VarpCLIConsole(
    private val cli: VarpCLI,
    private val commandManager: VarpCLICommandManager,
) : SimpleTerminalConsole(), VarpCLICommandSender {

    private val logger = LoggerFactory.getLogger(VarpCLIConsole::class.java)
    private val componentLogger = ComponentLogger.logger()

    override fun start() {
        thread(true, isDaemon = true, name = "console") {
            super.start()
        }
    }

    fun printHeader() {
        clear()
        sendMessage(" _____              ")
        sendMessage("|  |  |___ ___ ___    <color:#98C379>Version<gray>: <reset>${VarpBuildParameters.VERSION}")
        sendMessage("|  |  | .'|  _| . |   <color:#98C379>Branch<gray>: <reset>${VarpBuildParameters.GIT_BRANCH}")
        sendMessage(" \\___/|__,|_| |  _|   <color:#98C379>Commit<gray>: <reset>${VarpBuildParameters.GIT_COMMIT}")
        sendMessage("              |_|  ")
    }

    override fun shutdown() {
        cli.stop()
    }

    override fun isRunning(): Boolean {
        return true
    }

    override fun sendMessage(message: Component) {
        componentLogger.info(message)
    }

    fun clear() {
        val terminal = TerminalConsoleAppender.getTerminal() ?: return
        terminal.puts(InfoCmp.Capability.clear_screen)
        terminal.flush()
    }

    override fun runCommand(command: String) {
        runBlocking(cli.coroutineScope.coroutineContext) {
            try {
                commandManager.commandExecutor().executeCommand(this@VarpCLIConsole, command).await()
            } catch (exception: NoSuchCommandException) {
                logger.error(exception.message)
            } catch (exception: InvalidSyntaxException) {
                logger.error(exception.message)
            } catch (exception: ArgumentParseException) {
                logger.error(exception.cause.message)
            } catch (exception: CommandExecutionException) {
                val cause = exception.cause
                if (cause == null) {
                    logger.error("An error occurred while executing the last command.", exception)
                    return@runBlocking
                }

                when (cause) {
                    is FolderMoveIntoChildException -> logger.error(cause.message)
                    else -> logger.error("An error occurred while executing the last command.", cause)
                }
            } catch (exception: Exception) {
                logger.error("An error occurred whilst processing the last command.", exception)
            }
        }
    }

    override fun buildReader(builder: LineReaderBuilder): LineReader {
        builder.appName("VoxCloud")
        builder.completer(VarpCLIConsoleCompleter(this, commandManager))
        return super.buildReader(builder)
    }
}
