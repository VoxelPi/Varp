package net.voxelpi.varp.mod.fabric.client.gui.component

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.FlowLayout.Algorithm
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import net.kyori.adventure.text.Component
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.warp.path.FolderPath
import net.voxelpi.varp.warp.path.NodeParentPath
import net.voxelpi.varp.warp.path.RootPath

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
        Components.button(Text.literal("")) {
            selectPathAction(RootPath)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/home_button.png"), 0, 0, 16, 32))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(1))
        }

    val parentButton = Components.button(Text.literal("")) {
        val tempPath = path
        if (tempPath is FolderPath) {
            selectPathAction(tempPath.parent)
        }
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/up_button.png"), 0, 0, 16, 32))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.of(1))
    }

    val pathComponent = Components.label(Component.text(path.toString()).clientNative()).apply {
        margins(Insets.of(5, 0, 8, 0))
    }

    val createWarpButton = Components.button(Text.literal("")) {
        createWarpAction(path)
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/create_warp_button.png"), 0, 0, 16, 32))
        tooltip(Text.literal("Create a new warp"))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.vertical(1))
    }

    val createFolderButton = Components.button(Text.literal("")) {
        createFolderAction(path)
    }.apply {
        renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/create_folder_button.png"), 0, 0, 16, 32))
        tooltip(Text.literal("Create a new folder"))
        sizing(Sizing.fixed(16), Sizing.fixed(16))
        margins(Insets.vertical(1))
    }

    val rightSide = Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
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
