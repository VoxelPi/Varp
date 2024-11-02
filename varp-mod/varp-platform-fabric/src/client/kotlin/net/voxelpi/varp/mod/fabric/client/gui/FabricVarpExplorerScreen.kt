package net.voxelpi.varp.mod.fabric.client.gui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerMenuBar
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerTreeView
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath

class FabricVarpExplorerScreen(
    val tree: Tree,
    viewPath: NodeParentPath,
) : BaseOwoScreen<FlowLayout>() {

    var viewPath: NodeParentPath = viewPath
        private set

    private val menuBar: ExplorerMenuBar = ExplorerMenuBar(viewPath, Sizing.fill(100), Sizing.content()).apply {
        createWarpAction = {
            uiAdapter.toggleInspector()
        }
    }

    private val treeView = ExplorerTreeView(tree, Sizing.content(), Sizing.content(), this::changeViewContainer)

    private val body = object : FlowLayout(Sizing.fill(100), Sizing.fill(100), Algorithm.HORIZONTAL) {

        init {
            child(
                Containers.verticalScroll(Sizing.fixed(120), Sizing.fill(100), treeView).apply {
                    surface(Surface.DARK_PANEL)
                    padding(Insets.of(4, 4, 8, 4))
                    margins(Insets.of(0, 4, 4, 4))
                }
            )
        }

        override fun layout(space: Size?) {
            sizing(Sizing.fill(), Sizing.fill())
            super.layout(space)

            val maxHeight = fullSize().height() - menuBar.fullSize().height
            sizing(Sizing.fill(), Sizing.fixed(maxHeight))
            for (child in children()) {
                child.verticalSizing(Sizing.fixed(maxHeight))
            }

            super.layout(space)
        }
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)

        rootComponent.child(menuBar)
        rootComponent.child(body)

        changeViewPath(viewPath)
    }

    override fun shouldPause(): Boolean {
        return false
    }

    fun changeViewPath(path: NodeParentPath) {
        changeViewContainer(tree.resolve(path) ?: tree.root)
    }

    fun changeViewContainer(parent: NodeParent) {
        this.viewPath = parent.path

        menuBar.path = parent.path
    }
}