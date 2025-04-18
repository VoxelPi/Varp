package net.voxelpi.varp.mod.fabric.client.gui.screen

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.minecraft.client.MinecraftClient
import net.voxelpi.varp.mod.fabric.client.FabricVarpClient
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerContentList
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerMenuBar
import net.voxelpi.varp.mod.fabric.client.gui.component.ExplorerTreeView
import net.voxelpi.varp.warp.NodeParent
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.path.NodeParentPath

class FabricVarpExplorerScreen(
    viewPath: NodeParentPath,
) : BaseOwoScreen<FlowLayout>() {

    var viewPath: NodeParentPath = viewPath
        private set

    private val tree: Tree
        get() = FabricVarpClientMod.client.tree

    private val varpClient: FabricVarpClient
        get() = FabricVarpClientMod.client

    private val menuBar: ExplorerMenuBar = ExplorerMenuBar(viewPath, Sizing.fill(100), Sizing.content()).apply {
        selectPathAction = this@FabricVarpExplorerScreen::changeViewPath
        createWarpAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpCreateWarpScreen(it))
        }
        createFolderAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpCreateFolderScreen(it))
        }
    }

    private val treeView = ExplorerTreeView(Sizing.content(), Sizing.content(), this::changeViewContainer)

    private val contentList = ExplorerContentList(viewPath, Sizing.fill(100), Sizing.content()).apply {
        selectWarpAction = varpClient::teleportToWarp
        selectFolderAction = this@FabricVarpExplorerScreen::changeViewContainer

        editWarpAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpEditWarpScreen(it))
        }
        editFolderAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpEditFolderScreen(it))
        }
        deleteWarpAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpDeleteNodeScreen(it))
        }
        deleteFolderAction = {
            MinecraftClient.getInstance().setScreen(FabricVarpDeleteNodeScreen(it))
        }
    }

    private val body = object : FlowLayout(Sizing.fill(100), Sizing.fill(100), Algorithm.HORIZONTAL) {

        val treeSide = Containers.verticalScroll(Sizing.fixed(120), Sizing.fill(100), treeView).apply {
            surface(Surface.DARK_PANEL)
            padding(Insets.of(4, 4, 8, 4))
            margins(Insets.of(0, 4, 4, 4))
        }

        val contentSide = Containers.verticalScroll(Sizing.fill(), Sizing.fill(), contentList).apply {}

        init {
            child(treeSide)
            child(contentSide)
        }

        override fun layout(space: Size?) {
            sizing(Sizing.fill(), Sizing.fill())
            super.layout(space)

            val maxHeight = fullSize().height() - menuBar.fullSize().height - 4
            sizing(Sizing.fill(), Sizing.fixed(maxHeight))
            for (child in children()) {
                child.verticalSizing(Sizing.fixed(maxHeight))
            }
            contentSide.horizontalSizing(Sizing.fixed(fullSize().width - treeSide.fullSize().width))

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
        contentList.path = parent.path
    }
}
