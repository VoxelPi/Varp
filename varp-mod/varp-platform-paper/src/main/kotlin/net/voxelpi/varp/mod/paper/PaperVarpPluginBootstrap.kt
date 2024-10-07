package net.voxelpi.varp.mod.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import net.voxelpi.varp.mod.paper.command.PaperVarpCommandService

@Suppress("UnstableApiUsage")
class PaperVarpPluginBootstrap : PluginBootstrap {

    private lateinit var commandService: PaperVarpCommandService

    override fun bootstrap(context: BootstrapContext) {
        commandService = PaperVarpCommandService(context)
    }

    override fun createPlugin(context: PluginProviderContext): PaperVarpPlugin {
        return PaperVarpPlugin(commandService)
    }
}
