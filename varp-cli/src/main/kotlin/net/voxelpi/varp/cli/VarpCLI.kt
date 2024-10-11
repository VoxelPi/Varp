package net.voxelpi.varp.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.VarpImpl
import net.voxelpi.varp.cli.command.VarpCLICommandManager
import net.voxelpi.varp.cli.console.VarpCLIConsole
import net.voxelpi.varp.cli.coroutine.VarpCLIDispatcher
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object VarpCLI {

    init {
        VarpImpl.load()
    }

    private val logger = LoggerFactory.getLogger(VarpCLI::class.java)

    private val coroutineDispatcher = VarpCLIDispatcher()
    val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)

    val eventScope: EventScope = eventScope()

    val commandManager = VarpCLICommandManager(this)

    val console = VarpCLIConsole(this, commandManager)

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
