package net.voxelpi.varp.mod.fabric.client.gui

import net.minecraft.client.MinecraftClient
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.event.node.NodeCreateEvent
import net.voxelpi.varp.event.node.NodePathChangeEvent
import net.voxelpi.varp.event.node.NodePostDeleteEvent
import net.voxelpi.varp.event.node.NodeStateChangeEvent
import net.voxelpi.varp.event.repository.RepositoryLoadEvent
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.gui.screen.FabricVarpExplorerScreen
import net.voxelpi.varp.tree.Tree
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodePath
import net.voxelpi.varp.tree.path.RootPath
import net.voxelpi.varp.tree.path.WarpPath

class FabricVarpGUIListener(
    private val tree: Tree,
) {

    init {
        tree.eventScope.registerAnnotated(this)
    }

    fun cleanup() {
        tree.eventScope.unregisterAnnotated(this)
    }

    @Subscribe
    fun handle(event: NodeCreateEvent) {
        refreshScreenNode(event.node.path)
    }

    @Subscribe
    fun handle(event: NodePostDeleteEvent) {
        refreshScreenNode(event.path)
    }

    @Subscribe
    fun handle(event: NodePathChangeEvent) {
        refreshScreenNode(event.oldPath)
        refreshScreenNode(event.newPath)
    }

    @Subscribe
    fun handle(event: NodeStateChangeEvent) {
        refreshScreenNode(event.node.path)
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onStateSync(event: RepositoryLoadEvent) {
        refreshScreen()
    }

    private fun refreshScreen() {
        val screen = MinecraftClient.getInstance().currentScreen ?: return
        if (screen is FabricVarpExplorerScreen) {
            val node = tree.resolve(screen.viewPath)
            val newViewPath = node?.path ?: RootPath
            FabricVarpClientMod.client.openExplorer(newViewPath)
        }
    }

    private fun refreshScreenNode(path: NodePath) {
        val screen = MinecraftClient.getInstance().currentScreen ?: return
        if (screen is FabricVarpExplorerScreen) {
            when (path) {
                is FolderPath -> {
                    if (path.parent == screen.viewPath) {
                        val node = tree.resolve(screen.viewPath)
                        val newViewPath = node?.path ?: RootPath
                        FabricVarpClientMod.client.openExplorer(newViewPath)
                    }
                }
                is WarpPath -> {
                    if (path.parent == screen.viewPath) {
                        val node = tree.resolve(screen.viewPath)
                        val newViewPath = node?.path ?: RootPath
                        FabricVarpClientMod.client.openExplorer(newViewPath)
                    }
                }
                RootPath -> {
                    if (screen.viewPath == RootPath) {
                        FabricVarpClientMod.client.openExplorer(RootPath)
                    }
                }
            }
        }
    }
}
