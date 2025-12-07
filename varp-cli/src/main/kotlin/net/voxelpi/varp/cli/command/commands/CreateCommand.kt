package net.voxelpi.varp.cli.command.commands

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.voxelpi.event.annotation.Subscribe
import net.voxelpi.varp.MinecraftLocation
import net.voxelpi.varp.cli.command.CommandsRegistrationEvent
import net.voxelpi.varp.cli.command.VarpCLICommandSender
import net.voxelpi.varp.cli.util.valueFlag
import net.voxelpi.varp.extras.cloud.parser.keyParser
import net.voxelpi.varp.extras.cloud.parser.tree.nodeParentParser
import net.voxelpi.varp.tree.NodeParent
import net.voxelpi.varp.tree.state.FolderState
import net.voxelpi.varp.tree.state.WarpState
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.kotlin.extension.parser
import org.incendo.cloud.parser.standard.DoubleParser.doubleParser
import org.incendo.cloud.parser.standard.FloatParser.floatParser
import org.incendo.cloud.parser.standard.StringParser.quotedStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser

object CreateCommand {

    @Subscribe
    fun handle(event: CommandsRegistrationEvent) {
        event.commandManager.buildAndRegister("create") {
            literal("warp")
            required("parent", nodeParentParser { event.cli.tree })
            required("id", stringParser())

            required("world", keyParser())
            required("x", doubleParser())
            required("y", doubleParser())
            required("z", doubleParser())
            required("yaw", floatParser())
            required("pitch", floatParser())

            valueFlag<VarpCLICommandSender, String>("name", aliases = arrayOf("n")) {
                parser = quotedStringParser()
            }

            handler { context ->
                val parent: NodeParent = context["parent"]
                val id: String = context["id"]

                val world: Key = context["world"]
                val x: Double = context["x"]
                val y: Double = context["y"]
                val z: Double = context["z"]
                val yaw: Float = context["yaw"]
                val pitch: Float = context["pitch"]

                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))

                val state = WarpState(
                    MinecraftLocation(
                        world,
                        x,
                        y,
                        z,
                        yaw,
                        pitch,
                    ),
                    name,
                )
                runBlocking {
                    parent.createWarp(id, state).getOrThrow()
                }
            }
        }

        event.commandManager.buildAndRegister("create") {
            literal("folder")
            required("parent", nodeParentParser { event.cli.tree })
            required("id", stringParser())

            valueFlag<VarpCLICommandSender, String>("name", aliases = arrayOf("n")) {
                parser = quotedStringParser()
            }

            handler { context ->
                val parent: NodeParent = context["parent"]
                val id: String = context["id"]

                val name = miniMessage().deserialize(context.flags().getValue<String>("name").orElse(id))

                val state = FolderState(
                    name,
                )
                runBlocking {
                    parent.createFolder(id, state).getOrThrow()
                }
            }
        }
    }
}
