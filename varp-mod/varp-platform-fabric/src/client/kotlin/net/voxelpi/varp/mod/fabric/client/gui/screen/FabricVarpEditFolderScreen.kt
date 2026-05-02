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
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import net.voxelpi.varp.ComponentTemplate
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.tree.Folder

class FabricVarpEditFolderScreen(
    private var folder: Folder,
) : BaseOwoScreen<FlowLayout>() {

    override fun isPauseScreen(): Boolean = false

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow)
    }

    // For some reason without this code, all labels are compressed.
    private var stupidFix: Boolean = false
    private lateinit var menuPlane: FlowLayout

    override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        if (!stupidFix) {
            menuPlane.horizontalSizing(Sizing.fixed(1000))
            menuPlane.horizontalSizing(Sizing.fill(66))
            stupidFix = true
        }
        super.extractRenderState(context, mouseX, mouseY, delta)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        val idInput = UIComponents.textBox(Sizing.fill(60), folder.id).apply {
            setMaxLength(256)
            setEditable(false)
            margins(Insets.vertical(2))
        }

        val nameInput = UIComponents.textBox(Sizing.fill(60), folder.name.originalMessage).apply {
            setMaxLength(1024)
            margins(Insets.vertical(2))
        }

        menuPlane = UIContainers.verticalFlow(Sizing.fill(66), Sizing.content())
        menuPlane.child(
            UIContainers.horizontalFlow(Sizing.fill(95), Sizing.fixed(20)).apply {
                child(
                    UIComponents.texture(Identifier.parse("varp:textures/gui/edit_folder.png"), 0, 0, 16, 16, 16, 16).apply {
                        margins(Insets.of(0, 0, 0, 8))
                    }
                )
                child(
                    UIComponents.label(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.message")).apply {
                        horizontalTextAlignment(HorizontalAlignment.CENTER)
                        margins(Insets.of(0, 0, 0, 0))
                    }
                )
                horizontalAlignment(HorizontalAlignment.CENTER)
                verticalAlignment(VerticalAlignment.CENTER)
                margins(Insets.bottom(8))
            }
        )
        menuPlane.child(
            UIContainers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(
                    UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        child(
                            UIComponents.label(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.id")).apply {
                                horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                verticalTextAlignment(VerticalAlignment.CENTER)
                                sizing(Sizing.fill(25), Sizing.fixed(24))
                                margins(Insets.right(8))
                            }
                        )
                        child(idInput)
                    }
                )
                child(
                    UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        child(
                            UIComponents.label(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.name")).apply {
                                horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                verticalTextAlignment(VerticalAlignment.CENTER)
                                sizing(Sizing.fill(25), Sizing.fixed(24))
                                margins(Insets.right(8))
                            }
                        )
                        child(nameInput)
                    }
                )
                child(
                    UIContainers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                        child(
                            UIComponents.label(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.parent")).apply {
                                horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                verticalTextAlignment(VerticalAlignment.CENTER)
                                sizing(Sizing.fill(25), Sizing.fixed(24))
                                margins(Insets.right(8))
                            }
                        )
                        child(
                            UIComponents.button(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.select_parent")) {}.apply {
                                horizontalSizing(Sizing.fill(60))
                                margins(Insets.vertical(2))
                            }
                        )
                    }
                )
            }
        )
        menuPlane.child(
            UIContainers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(
                    UIComponents.button(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.cancel")) {
                        FabricVarpClientMod.client.openExplorer(folder.parent.path)
                    }.apply {
                        margins(Insets.horizontal(4))
                        horizontalSizing(Sizing.fill(45))
                    }
                )
                child(
                    UIComponents.button(net.minecraft.network.chat.Component.translatable("gui.varp.edit_folder.confirm")) {
                        val id = idInput.value
                        val name = ComponentTemplate(nameInput.value)
                        runBlocking {
                            folder.modify {
                                this.name = name
                            }
                        }

                        // Open parent in explorer gui.
                        FabricVarpClientMod.client.openExplorer(folder.parent.path)
                    }.apply {
                        margins(Insets.horizontal(4))
                        horizontalSizing(Sizing.fill(45))
                    }
                )
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                margins(Insets.top(8))
            }
        )
        menuPlane.surface(Surface.DARK_PANEL)
        menuPlane.padding(Insets.of(6, 8, 8, 8))
        menuPlane.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        rootComponent.child(menuPlane)
    }
}
