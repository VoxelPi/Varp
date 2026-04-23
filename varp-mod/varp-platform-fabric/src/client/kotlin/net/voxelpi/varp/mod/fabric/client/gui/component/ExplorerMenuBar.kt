package net.voxelpi.varp.mod.fabric.client.gui.component

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.kyori.adventure.text.Component
import net.minecraft.resources.Identifier
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.tree.path.FolderPath
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.path.RootPath

/**
 * Menu bar of the explorer screen
 */
class ExplorerMenuBar(
    initialPath: NodeParentPath,
    horizontalSizing: Sizing,
    verticalSizing: Sizing,
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {

    var path: NodeParentPath = initialPath
        set(value) {
            field = value

            // Update components.
            pathComponent.text(Component.text(value.toString()).clientNative())
        }

    var selectPathAction: ((path: NodeParentPath) -> Unit) = {}

    var createWarpAction: ((path: NodeParentPath) -> Unit) = {}

    var createFolderAction: ((path: NodeParentPath) -> Unit) = {}

    val rootButton =
        UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
            selectPathAction(RootPath)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/home_button.png"), 0, 0, 16, 32))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(1))
        }

    val parentButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
        val tempPath = path
        if (tempPath is FolderPath) {
            selectPathAction(tempPath.parent)
        }
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/up_button.png"), 0, 0, 16, 32))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.of(1))
    }

    val pathComponent = UIComponents.label(Component.text(path.toString()).clientNative()).apply {
        margins(Insets.of(5, 0, 8, 0))
    }

    val createWarpButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
        createWarpAction(path)
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/create_warp_button.png"), 0, 0, 16, 32))
        tooltip(net.minecraft.network.chat.Component.literal("Create a new warp"))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.vertical(1))
    }

    val createFolderButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
        createFolderAction(path)
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/create_folder_button.png"), 0, 0, 16, 32))
        tooltip(net.minecraft.network.chat.Component.literal("Create a new folder"))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.vertical(1))
    }

    val rightSide = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
        child(createWarpButton)
        child(createFolderButton)

        gap(4)
        padding(Insets.horizontal(4))
    }

    init {
        child(rootButton)
        child(parentButton)
        child(pathComponent)
        child(rightSide)

        surface(Surface.DARK_PANEL)
        padding(Insets.of(4))
        margins(Insets.of(4))
    }

    override fun layout(space: Size) {
        super.layout(space)
        rightSide.positioning(
            Positioning.absolute(fullSize().width - padding().get().horizontal() - margins().get().horizontal() - rightSide.fullSize().width, 0)
        )
        super.layout(space)
    }
}
