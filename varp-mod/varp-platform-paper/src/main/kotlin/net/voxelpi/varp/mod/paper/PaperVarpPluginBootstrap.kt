package net.voxelpi.varp.mod.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext

@Suppress("UnstableApiUsage")
class PaperVarpPluginBootstrap : PluginBootstrap {

    override fun bootstrap(context: BootstrapContext) {}

    override fun createPlugin(context: PluginProviderContext): PaperVarpPlugin {
        return PaperVarpPlugin()
    }
}
