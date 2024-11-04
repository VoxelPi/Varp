package net.voxelpi.varp.mod.fabric.client.gui.component

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.Component.FocusSource
import io.wispforest.owo.ui.core.CursorStyle
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.ParentComponent
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.Delta
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.text.Text
import net.minecraft.util.math.RotationAxis
import net.voxelpi.varp.mod.fabric.client.FabricVarpClientMod
import net.voxelpi.varp.mod.fabric.client.util.clientNative
import net.voxelpi.varp.warp.Folder
import net.voxelpi.varp.warp.NodeParent
import org.lwjgl.glfw.GLFW
import kotlin.collections.sortedBy

class ExplorerTreeView(
    horizontalSizing: Sizing,
    verticalSizing: Sizing,
    val selectionCallback: (node: NodeParent) -> Unit,
) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.VERTICAL) {

    init {
        updateContent()
    }

    fun updateContent() {
        clearChildren()
        child(Entry(FabricVarpClientMod.client.tree.root, selectionCallback))
    }

    class Entry(
        private val node: NodeParent,
        private val selectionCallback: (node: NodeParent) -> Unit,
    ) : FlowLayout(Sizing.content(), Sizing.content(), Algorithm.VERTICAL) {

        private val spinnyBoiComponent: SpinnyBoiComponent
        private val headerLayout: FlowLayout
        private val bodyLayout: FlowLayout

        var expanded: Boolean = true

        private var content: MutableList<Component> = mutableListOf()

        init {
            spinnyBoiComponent = SpinnyBoiComponent()
            spinnyBoiComponent.targetRotation = if (expanded) 90f else 0f
            spinnyBoiComponent.rotation = spinnyBoiComponent.targetRotation

            headerLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
            headerLayout.padding(Insets.of(3, 3, 0, 0))
            headerLayout.child(
                spinnyBoiComponent
            )
            headerLayout.child(
                Components.label(node.name.clientNative())
            )
            super.child(headerLayout)

            bodyLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
            bodyLayout.padding(Insets.left(8))
            bodyLayout.surface(SURFACE)
            super.child(bodyLayout)

            for (folder in node.childFolders().sortedBy(Folder::id)) {
                content.add(Entry(folder, selectionCallback))
            }
            if (expanded) {
                bodyLayout.children(content)
            }
        }

        fun toggleExpansion() {
            if (expanded) {
                bodyLayout.clearChildren()
                spinnyBoiComponent.targetRotation = 0f
            } else {
                bodyLayout.children(content)
                spinnyBoiComponent.targetRotation = 90f
            }

            expanded = !expanded
        }

        override fun canFocus(source: FocusSource): Boolean {
            return source == FocusSource.KEYBOARD_CYCLE
        }

        override fun onKeyPress(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                toggleExpansion()
                super.onKeyPress(keyCode, scanCode, modifiers)
                return true
            }

            return super.onKeyPress(keyCode, scanCode, modifiers)
        }

        override fun onMouseDown(mouseX: Double, mouseY: Double, button: Int): Boolean {
            val superResult = super.onMouseDown(mouseX, mouseY, button)

            return if (mouseY <= this.headerLayout.fullSize().height && !superResult) {
                if (mouseX <= this.spinnyBoiComponent.fullSize().width) {
                    toggleExpansion()
                    UISounds.playInteractionSound()
                } else {
                    selectionCallback.invoke(node)
                    UISounds.playButtonSound()
                }
                true
            } else {
                superResult
            }
        }

        class SpinnyBoiComponent : LabelComponent(Text.literal(">")) {
            var rotation: Float = 90f
            var targetRotation: Float = 90f

            init {
                margins(Insets.of(0, 0, 4, 4))
                cursorStyle(CursorStyle.HAND)
            }

            override fun update(delta: Float, mouseX: Int, mouseY: Int) {
                super.update(delta, mouseX, mouseY)
                rotation += Delta.compute(rotation, targetRotation, delta * 0.65f)
            }

            override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
                val matrices = context.matrices

                matrices.push()
                matrices.translate(x + width / 2f - 1, y + height / 2f - 1, 0f)
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation))
                matrices.translate(-(x + width / 2f - 1), -(y + height / 2f - 1), 0f)

                super.draw(context, mouseX, mouseY, partialTicks, delta)
                matrices.pop()
            }
        }

        companion object {
            private val SURFACE = Surface { context: OwoUIDrawContext, component: ParentComponent ->
                context.fill(
                    component.x() + 4,
                    component.y(),
                    component.x() + 5,
                    component.y() + component.height(),
                    0x77FFFFFF
                )
            }
        }
    }
}
