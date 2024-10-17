package net.voxelpi.varp.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.minimessage.MiniMessage
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.cli.command.VarpCLICommandManager
import net.voxelpi.varp.cli.console.VarpCLIConsole
import net.voxelpi.varp.cli.coroutine.VarpCLIDispatcher
import net.voxelpi.varp.repository.filetree.FileTreeTreeRepository
import net.voxelpi.varp.repository.filetree.RepositoryFileFormat
import net.voxelpi.varp.warp.Tree
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.system.exitProcess

object VarpCLI {

    private val logger = LoggerFactory.getLogger(VarpCLI::class.java)

    private val coroutineDispatcher = VarpCLIDispatcher()
    val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)

    val eventScope: EventScope = eventScope()

    val commandManager = VarpCLICommandManager(this)

    val console = VarpCLIConsole(this, commandManager)

    val repository = FileTreeTreeRepository("default", Path("repositories/default"), RepositoryFileFormat.JSON, MiniMessage.miniMessage())

    var tree: Tree = Varp.createTree(repository)

    fun start() {
        console.start()
        console.printHeader()

        while (true) {
            coroutineDispatcher.executor.runTasks()
            Thread.sleep(10)
        }
    }

    fun stop() {
        exitProcess(0)
    }
}
