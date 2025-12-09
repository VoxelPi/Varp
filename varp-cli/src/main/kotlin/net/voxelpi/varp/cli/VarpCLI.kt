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
import net.voxelpi.varp.environment.VarpEnvironment
import net.voxelpi.varp.environment.VarpEnvironmentLoader
import net.voxelpi.varp.environment.model.EnvironmentDefinition
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryConfig
import net.voxelpi.varp.repository.filetree.FileTreeRepositoryType
import net.voxelpi.varp.repository.sql.SqlRepositoryType
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.RootPath
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

    val environmentLoader = VarpEnvironmentLoader.withStandardTypes(
        listOf(FileTreeRepositoryType, SqlRepositoryType.PostgreSql, SqlRepositoryType.MySql),
    )

    val defaultEnvironment = EnvironmentDefinition.environmentDefinition {
        repository("default", FileTreeRepositoryType, FileTreeRepositoryConfig(Path("./default/"), "json", false)) {
            mountedAt(RootPath)
        }
    }

    val environment = runBlocking { VarpEnvironment.environment(defaultEnvironment).getOrThrow() }

    val tree: Tree
        get() = environment.tree

    fun start() {
        console.start()
        console.printHeader()

        val definition = environmentLoader.load(Path("varp.json").toAbsolutePath().normalize()).getOrElse {
            logger.error("Unable to load tree: ${it.message}", it)
            stop()
        } ?: defaultEnvironment
        runBlocking {
            environment.load(definition)
        }
        logger.info("Loaded ${environment.repositories.size} repositories")
        logger.info("Loaded ${environment.compositor.mounts().size} mounts")

        while (true) {
            coroutineDispatcher.executor.runTasks()
            Thread.sleep(10)
        }
    }

    fun stop(): Nothing {
        runBlocking {
            val definition = environment.save()
            environmentLoader.save(definition, Path("varp.json").toAbsolutePath().normalize())
            environment.deactivate()
        }
        coroutineScope.cancel()
        exitProcess(0)
    }
}
