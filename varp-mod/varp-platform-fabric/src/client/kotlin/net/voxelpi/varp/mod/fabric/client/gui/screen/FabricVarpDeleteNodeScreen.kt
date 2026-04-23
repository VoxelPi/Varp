package net.voxelpi.varp.mod.fabric.client.gui.screen

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.UIComponents
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.UIContainers
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import kotlinx.coroutines.runBlocking
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.tree.NodeChild
import net.voxelpi.varp.tree.Warp

class FabricVarpDeleteNodeScreen(
    val node: NodeChild,
) : BaseOwoScreen<FlowLayout>() {

    override fun isPauseScreen(): Boolean = false

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        rootComponent.child(
            UIContainers.verticalFlow(Sizing.fill(50), Sizing.content()).apply {
                child(
                    UIComponents.label(net.minecraft.network.chat.Component.translatable("gui.varp.delete_node.message", if (node is Warp) "warp" else "folder")).apply {
                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                        sizing(Sizing.fill(90), Sizing.content())
                        margins(Insets.bottom(8))
                    }
                )
                child(
                    UIComponents.label(node.name.clientNative()).apply {
                        margins(Insets.bottom(8))
                    }
                )
                child(
                    UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                        child(
                            UIComponents.button(net.minecraft.network.chat.Component.translatable("gui.varp.delete_node.cancel")) {
                                FabricVarpClientMod.client.openExplorer(node.parent.path)
                            }.apply {
                                margins(Insets.horizontal(4))
                                horizontalSizing(Sizing.fill(45))
                            }
                        )
                        child(
                            UIComponents.button(net.minecraft.network.chat.Component.translatable("gui.varp.delete_node.confirm")) {
                                runBlocking { node.delete() }
                                FabricVarpClientMod.client.openExplorer(node.parent.path)
                            }.apply {
                                margins(Insets.horizontal(4))
                                horizontalSizing(Sizing.fill(45))
                            }
                        )
                        alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    }
                )
                surface(Surface.DARK_PANEL)
                padding(Insets.both(8, 8))
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
            }
        )
    }
}
