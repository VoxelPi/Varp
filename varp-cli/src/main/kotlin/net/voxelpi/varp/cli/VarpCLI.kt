package net.voxelpi.varp.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.cli.command.VarpCLICommandManager
import net.voxelpi.varp.cli.console.VarpCLIConsole
import net.voxelpi.varp.cli.coroutine.VarpCLIDispatcher
import net.voxelpi.varp.loader.VarpLoader
import net.voxelpi.varp.repository.filetree.FileTreeRepository
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

    val loader = VarpLoader.loader(Path(".")) {
        registerRepositoryType<FileTreeRepository>()
    }

    var tree: Tree = loader.tree

    fun start() {
        console.start()
        console.printHeader()

        runBlocking {
            loader.load().getOrElse {
                logger.error("Unable to load tree: ${it.message}", it)
                stop()
            }
        }
        logger.info("Loaded ${loader.repositories().size} repositories")
        logger.info("Loaded ${loader.compositor.mounts().size} mounts")

        while (true) {
            coroutineDispatcher.executor.runTasks()
            Thread.sleep(10)
        }
    }

    fun stop() {
        runBlocking {
            loader.save()
            loader.cleanup()
        }
        coroutineScope.cancel()
        exitProcess(0)
    }
}
