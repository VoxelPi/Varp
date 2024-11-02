package net.voxelpi.varp.mod.fabric.client.gui.component

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.UISounds
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.NodeChild
import net.voxelpi.varp.warp.Tree
import net.voxelpi.varp.warp.Warp
import net.voxelpi.varp.warp.path.NodeParentPath

class ExplorerContentList(
    val tree: Tree,
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

        init {
            child(
                Components.texture(Identifier.of("varp:textures/gui/folder.png"), 0, 0, 16, 16, 16, 16).apply {
                    margins(Insets.of(2, 0, 0, 4))
                }
            )
            child(
                Components.label(
                    folder.name.hoverEvent(Component.text(folder.path.value)).decorate(TextDecoration.BOLD).clientNative()
                ).apply {
                    sizing(Sizing.fixed(160), Sizing.content())
                    margins(Insets.top(5))
                }
            )

            child(
                Components.button(Text.literal("")) {
                    editAction.invoke(folder)
                }.apply {
                    renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/edit_button.png"), 0, 0, 16, 32))
                    tooltip(Text.literal("Edit the folder"))
                    sizing(Sizing.fixed(16), Sizing.fixed(16))
                    margins(Insets.of(2, 0, 0, 0))
                }
            )
            child(
                Components.button(Text.literal("")) {
                    deleteAction.invoke(folder)
                }.apply {
                    renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/delete_button.png"), 0, 0, 16, 32))
                    tooltip(Text.literal("Delete the folder"))
                    sizing(Sizing.fixed(16), Sizing.fixed(16))
                    margins(Insets.of(2, 0, 0, 0))
                }
            )

            surface(Surface.DARK_PANEL)
            padding(Insets.of(0, 0, 4, 0))
            margins(Insets.bottom(2))
        }

        override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!super.onMouseDown(mouseX, mouseY, button)) {
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

        init {
            child(
                Components.texture(Identifier.of("varp:textures/gui/warp.png"), 0, 0, 16, 16, 16, 16).apply {
                    margins(Insets.of(2, 0, 0, 4))
                }
            )
            child(
                Components.label(
                    warp.name.hoverEvent(Component.text(warp.path.value)).clientNative()
                ).apply {
                    sizing(Sizing.fixed(160), Sizing.content())
                    margins(Insets.top(5))
                }
            )

            child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                    child(
                        Components.label(
                            Component.text(warp.location.world.asString()).clientNative()
                        ).apply {
                            sizing(Sizing.fixed(120), Sizing.content())
                            margins(Insets.top(5))
                        }
                    )
                    child(
                        Components.label(
                            Component.text("%.2f".format(warp.location.x)).color(NamedTextColor.RED).clientNative()
                        ).apply {
                            horizontalTextAlignment(HorizontalAlignment.RIGHT)
                            sizing(Sizing.fixed(48), Sizing.content())
                            margins(Insets.top(5))
                        }
                    )
                    child(
                        Components.label(
                            Component.text("%.2f".format(warp.location.y)).color(NamedTextColor.GREEN).clientNative()
                        ).apply {
                            horizontalTextAlignment(HorizontalAlignment.RIGHT)
                            sizing(Sizing.fixed(48), Sizing.content())
                            margins(Insets.top(5))
                        }
                    )
                    child(
                        Components.label(
                            Component.text("%.2f".format(warp.location.z)).color(NamedTextColor.BLUE).clientNative()
                        ).apply {
                            horizontalTextAlignment(HorizontalAlignment.RIGHT)
                            sizing(Sizing.fixed(48), Sizing.content())
                            margins(Insets.top(5))
                        }
                    )
                }
            )

            child(
                Components.button(Text.literal("")) {
                    editAction.invoke(warp)
                }.apply {
                    renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/edit_button.png"), 0, 0, 16, 32))
                    tooltip(Text.literal("Edit the warp"))
                    sizing(Sizing.fixed(16), Sizing.fixed(16))
                    margins(Insets.of(2, 0, 0, 0))
                }
            )
            child(
                Components.button(Text.literal("")) {
                    deleteAction.invoke(warp)
                }.apply {
                    renderer(ButtonComponent.Renderer.texture(Identifier.of("varp:textures/gui/delete_button.png"), 0, 0, 16, 32))
                    tooltip(Text.literal("Delete the warp"))
                    sizing(Sizing.fixed(16), Sizing.fixed(16))
                    margins(Insets.of(2, 0, 0, 0))
                }
            )

            surface(Surface.DARK_PANEL)
            padding(Insets.of(0, 0, 4, 0))
            margins(Insets.bottom(2))
        }

        override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
            if (!super.onMouseDown(mouseX, mouseY, button)) {
                UISounds.playButtonSound()
                selectAction.invoke(warp)
                return true
            } else {
                return false
            }
        }
    }
}
