package net.voxelpi.varp.mod.fabric.client.gui.component

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.UISounds
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.tree.Folder
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.Warp
import net.voxelpi.varp.tree.path.NodeParentPath

class ExplorerContentList(
    path: NodeParentPath,
    horizontalSizing: Sizing,
    verticalSizing: Sizing,
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {

    var path: NodeParentPath = path
        set(value) {
            field = value
            updateContent()
        }

    var selectFolderAction: ((Folder) -> Unit) = {}
    var selectWarpAction: ((Warp) -> Unit) = {}
    var editFolderAction: ((Folder) -> Unit) = {}
    var editWarpAction: ((Warp) -> Unit) = {}
    var deleteFolderAction: ((Folder) -> Unit) = {}
    var deleteWarpAction: ((Warp) -> Unit) = {}

    init {
        padding(Insets.right(4))
    }

    private fun updateContent() {
        clearChildren()

        val tree = FabricVarpClientMod.client.tree

        val container = tree.resolve(path) ?: return

        for (folder in container.childFolders().sortedBy(NodeChild::id)) {
            child(
                FolderEntry(folder, Sizing.fill(100), Sizing.fixed(20), selectFolderAction, editFolderAction, deleteFolderAction),
            )
        }
        for (warp in container.childWarps().sortedBy(NodeChild::id)) {
            child(
                WarpEntry(warp, Sizing.fill(100), Sizing.fixed(20), selectWarpAction, editWarpAction, deleteWarpAction),
            )
        }
    }

    class FolderEntry(
        val folder: Folder,
        horizontalSizing: Sizing,
        verticalSizing: Sizing,
        var selectAction: ((Folder) -> Unit),
        var editAction: ((Folder) -> Unit),
        var deleteAction: ((Folder) -> Unit),
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {

        private val iconTexture = UIComponents.texture(Identifier.parse("varp:textures/gui/folder.png"), 0, 0, 16, 16, 16, 16).apply {
            margins(Insets.of(2, 0, 0, 4))
        }

        private val nameLabel = UIComponents.label(
            folder.name.asComponent().hoverEvent(Component.text(folder.path.value)).decorate(TextDecoration.BOLD).clientNative()
        ).apply {
            sizing(Sizing.fixed(160), Sizing.content())
            margins(Insets.top(5))
        }

        private val editButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
            editAction.invoke(folder)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/edit_button.png"), 0, 0, 16, 32))
            tooltip(net.minecraft.network.chat.Component.literal("Edit the folder"))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(2, 0, 0, 0))
        }

        private val deleteButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
            deleteAction.invoke(folder)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/delete_button.png"), 0, 0, 16, 32))
            tooltip(net.minecraft.network.chat.Component.literal("Delete the folder"))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(2, 0, 0, 0))
        }

        private val rightSide = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            child(editButton)
            child(deleteButton)

            gap(4)
            padding(Insets.horizontal(4))
        }

        init {
            child(iconTexture)
            child(nameLabel)
            child(rightSide)

            surface(Surface.DARK_PANEL)
            padding(Insets.of(0, 0, 4, 4))
            margins(Insets.bottom(2))
        }

        override fun layout(space: Size) {
            super.layout(space)

            rightSide.positioning(
                Positioning.absolute(fullSize().width - padding().get().horizontal() - margins().get().horizontal() - rightSide.fullSize().width, 0)
            )

            super.layout(space)
        }

        override fun onMouseDown(click: MouseButtonEvent?, doubled: Boolean): Boolean {
            if (!super.onMouseDown(click, doubled)) {
                UISounds.playButtonSound()
                selectAction.invoke(folder)
                return true
            } else {
                return false
            }
        }
    }

    class WarpEntry(
        val warp: Warp,
        horizontalSizing: Sizing,
        verticalSizing: Sizing,
        var selectAction: ((Warp) -> Unit),
        var editAction: ((Warp) -> Unit),
        var deleteAction: ((Warp) -> Unit),
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {

        private val iconTexture = UIComponents.texture(Identifier.parse("varp:textures/gui/warp.png"), 0, 0, 16, 16, 16, 16).apply {
            margins(Insets.of(2, 0, 0, 4))
        }

        private val nameLabel = UIComponents.label(
            warp.name.asComponent().hoverEvent(Component.text(warp.path.value)).clientNative()
        ).apply {
            sizing(Sizing.fixed(160), Sizing.content())
            margins(Insets.top(5))
        }

        private val infoLabel = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            child(
                UIComponents.label(
                    Component.text(warp.location.world.asString()).clientNative()
                ).apply {
                    sizing(Sizing.fixed(120), Sizing.content())
                    margins(Insets.top(5))
                }
            )
            child(
                UIComponents.label(
                    Component.text("%.2f".format(warp.location.x)).color(NamedTextColor.RED).clientNative()
                ).apply {
                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                    sizing(Sizing.fixed(48), Sizing.content())
                    margins(Insets.top(5))
                }
            )
            child(
                UIComponents.label(
                    Component.text("%.2f".format(warp.location.y)).color(NamedTextColor.GREEN).clientNative()
                ).apply {
                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                    sizing(Sizing.fixed(48), Sizing.content())
                    margins(Insets.top(5))
                }
            )
            child(
                UIComponents.label(
                    Component.text("%.2f".format(warp.location.z)).color(NamedTextColor.BLUE).clientNative()
                ).apply {
                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                    sizing(Sizing.fixed(48), Sizing.content())
                    margins(Insets.top(5))
                }
            )
        }

        private val editButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
            editAction.invoke(warp)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/edit_button.png"), 0, 0, 16, 32))
            tooltip(net.minecraft.network.chat.Component.literal("Edit the warp"))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(2, 0, 0, 0))
        }

        private val deleteButton = UIComponents.button(net.minecraft.network.chat.Component.literal("")) {
            deleteAction.invoke(warp)
        }.apply {
            renderer(ButtonComponent.Renderer.texture(Identifier.parse("varp:textures/gui/delete_button.png"), 0, 0, 16, 32))
            tooltip(net.minecraft.network.chat.Component.literal("Delete the warp"))
            sizing(Sizing.fixed(16), Sizing.fixed(16))
            margins(Insets.of(2, 0, 0, 0))
        }

        private val rightSide = UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            child(editButton)
            child(deleteButton)

            gap(4)
            padding(Insets.horizontal(4))
        }

        init {
            child(iconTexture)
            child(nameLabel)
            child(infoLabel)
            child(rightSide)

            surface(Surface.DARK_PANEL)
            padding(Insets.of(0, 0, 4, 4))
            margins(Insets.bottom(2))
        }

        override fun layout(space: Size) {
            super.layout(space)

            rightSide.positioning(
                Positioning.absolute(fullSize().width - padding().get().horizontal() - margins().get().horizontal() - rightSide.fullSize().width, 0)
            )

            // Hide info if there is no room for it.
            if (width() - padding().get().horizontal() - rightSide.fullSize().width() - iconTexture.fullSize().width() - nameLabel.fullSize().width() < infoLabel.fullSize().width()) {
                infoLabel.verticalSizing(Sizing.fixed(0))
            } else {
                infoLabel.verticalSizing(Sizing.content())
            }

            super.layout(space)
        }

        override fun onMouseDown(click: MouseButtonEvent?, doubled: Boolean): Boolean {
            if (!super.onMouseDown(click, doubled)) {
                UISounds.playButtonSound()
                selectAction.invoke(warp)
                return true
            } else {
                return false
            }
        }
    }
}
