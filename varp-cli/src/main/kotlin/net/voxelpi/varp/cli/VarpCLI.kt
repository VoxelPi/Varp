package net.voxelpi.varp.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.event.EventScope
import net.voxelpi.event.eventScope
import net.voxelpi.varp.Varp
import net.voxelpi.varp.cli.command.VarpCLICommandManager
import net.voxelpi.varp.cli.console.VarpCLIConsole
import net.voxelpi.varp.cli.coroutine.VarpCLIDispatcher
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.provider.registry.TreeRegistry
import net.voxelpi.varp.warp.state.FolderState
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object VarpCLI {

    private val logger = LoggerFactory.getLogger(VarpCLI::class.java)

    private val coroutineDispatcher = VarpCLIDispatcher()
    val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)

    val eventScope: EventScope = eventScope()

    val commandManager = VarpCLICommandManager(this)

    val console = VarpCLIConsole(this, commandManager)

    var tree: Tree = Varp.createTree(TreeRegistry())

    init {
        val folder1 = tree.root.createFolder("folder1", FolderState(Component.text("test1"))).getOrThrow()
        val folder2 = tree.root.createFolder("folder2", FolderState(Component.text("test2"))).getOrThrow()
        val folder3 = folder1.createFolder("folder3", FolderState(Component.text("test3"))).getOrThrow()
        val folder4 = folder3.createFolder("folder4", FolderState(Component.text("test4"))).getOrThrow()
        val folder5 = folder2.createFolder("folder5", FolderState(miniMessage().deserialize("<underlined>This text is underlined</underlined>"))).getOrThrow()
        val folder6 = folder3.createFolder("folder6", FolderState(miniMessage().deserialize("<rainbow>THIS IS A TEST</rainbow>"))).getOrThrow()
    }

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
