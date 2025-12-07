package net.voxelpi.varp.mod.fabric.client.gui.screen

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.tree.path.NodeParentPath
import net.voxelpi.varp.tree.state.WarpState
import java.util.Locale

class FabricVarpCreateWarpScreen(
    private var parentPath: NodeParentPath,
) : BaseOwoScreen<FlowLayout>() {

    override fun shouldPause(): Boolean = false

    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this, Containers::verticalFlow)
    }

    // For some reason without this code, all labels are compressed.
    private var stupidFix: Boolean = false
    private lateinit var menuPlane: FlowLayout

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (!stupidFix) {
            menuPlane.horizontalSizing(Sizing.fixed(1000))
            menuPlane.horizontalSizing(Sizing.fill(66))
            stupidFix = true
        }
        super.render(context, mouseX, mouseY, delta)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT)
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

        val idInput = Components.textBox(Sizing.fill(60), "warp").apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val nameInput = Components.textBox(Sizing.fill(60), "Warp").apply {
            setMaxLength(1024)
            margins(Insets.vertical(2))
        }

        val worldInput = Components.textBox(Sizing.fill(60), MinecraftClient.getInstance().world?.registryKey?.value?.asString()).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val xInput = Components.textBox(Sizing.fill(60), "%.3f".format(Locale.ENGLISH, MinecraftClient.getInstance().player?.x ?: 0.0)).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val yInput = Components.textBox(Sizing.fill(60), "%.3f".format(Locale.ENGLISH, MinecraftClient.getInstance().player?.y ?: 0.0)).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val zInput = Components.textBox(Sizing.fill(60), "%.3f".format(Locale.ENGLISH, MinecraftClient.getInstance().player?.z ?: 0.0)).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val yawInput = Components.textBox(Sizing.fill(60), "%.3f".format(Locale.ENGLISH, MinecraftClient.getInstance().player?.yaw ?: 0f)).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        val pitchInput = Components.textBox(Sizing.fill(60), "%.3f".format(Locale.ENGLISH, MinecraftClient.getInstance().player?.pitch ?: 0f)).apply {
            setMaxLength(256)
            margins(Insets.vertical(2))
        }

        menuPlane = Containers.verticalFlow(Sizing.fill(66), Sizing.content())
        menuPlane.child(
            Containers.horizontalFlow(Sizing.fill(95), Sizing.fixed(20)).apply {
                child(
                    Components.texture(Identifier.of("varp:textures/gui/create_warp.png"), 0, 0, 16, 16, 16, 16).apply {
                        margins(Insets.of(0, 0, 0, 8))
                    }
                )
                child(
                    Components.label(Text.translatable("gui.varp.create_warp.message")).apply {
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
            Containers.verticalScroll(
                Sizing.fill(100),
                Sizing.fill(60),
                Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.id")).apply {
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
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.name")).apply {
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
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.parent")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(
                                Components.button(Text.translatable("gui.varp.create_warp.select_parent")) {}.apply {
                                    horizontalSizing(Sizing.fill(60))
                                    margins(Insets.vertical(2))
                                }
                            )
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.world")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(worldInput)
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.x")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(xInput)
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.y")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(yInput)
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.z")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(zInput)
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.yaw")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(yawInput)
                        }
                    )
                    child(
                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                            child(
                                Components.label(Text.translatable("gui.varp.create_warp.pitch")).apply {
                                    horizontalTextAlignment(HorizontalAlignment.RIGHT)
                                    verticalTextAlignment(VerticalAlignment.CENTER)
                                    sizing(Sizing.fill(25), Sizing.fixed(24))
                                    margins(Insets.right(8))
                                }
                            )
                            child(pitchInput)
                        }
                    )
                }
            )
        )
        menuPlane.child(
            Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(
                    Components.button(Text.translatable("gui.varp.create_warp.cancel")) {
                        FabricVarpClientMod.client.openExplorer(parentPath)
                    }.apply {
                        margins(Insets.horizontal(4))
                        horizontalSizing(Sizing.fill(45))
                    }
                )
                child(
                    Components.button(Text.translatable("gui.varp.create_warp.confirm")) {
                        val id = idInput.text
                        val path = parentPath.warp(id)
                        val name = MiniMessage.miniMessage().deserialize(nameInput.text)
                        val location = MinecraftLocation(
                            Key.key(worldInput.text),
                            xInput.text.toDouble(),
                            yInput.text.toDouble(),
                            zInput.text.toDouble(),
                            yawInput.text.toFloat(),
                            pitchInput.text.toFloat(),
                        )
                        runBlocking { FabricVarpClientMod.client.tree.createWarp(path, WarpState(location, name)) } // Only sends packet.

                        // Open parent in explorer gui.
                        FabricVarpClientMod.client.openExplorer(parentPath)
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
