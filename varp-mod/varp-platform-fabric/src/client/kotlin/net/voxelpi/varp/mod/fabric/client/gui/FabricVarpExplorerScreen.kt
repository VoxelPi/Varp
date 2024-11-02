package net.voxelpi.varp.mod.fabric.client.gui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerMenuBar
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath

class FabricVarpExplorerScreen(
    val tree: Tree,
    viewPath: NodeParentPath,
) : BaseOwoScreen<FlowLayout>() {

    var viewPath: NodeParentPath = viewPath
        private set

    private val menuBar: ExplorerMenuBar = ExplorerMenuBar(viewPath, Sizing.fill(100), Sizing.content())

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)

        rootComponent.child(menuBar)

        changeViewPath(viewPath)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    fun changeViewPath(path: NodeParentPath) {
        this.viewPath = path

        menuBar.path = path
    }
}
